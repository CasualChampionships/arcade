/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.MinigameState
import net.casual.arcade.minigame.component.SerializableMinigameComponent
import net.casual.arcade.minigame.exception.MinigameCreationException
import net.casual.arcade.minigame.exception.MinigameSerializationException
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.serialization.codec.setOf
import net.casual.arcade.utils.serialization.json.JsonValueInput
import net.casual.arcade.utils.serialization.json.JsonValueOutput
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import net.minecraft.server.MinecraftServer
import net.minecraft.server.players.NameAndId
import net.minecraft.util.ProblemReporter
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.nio.file.AtomicMoveNotSupportedException
import java.nio.file.Path
import java.nio.file.StandardCopyOption
import java.util.*
import kotlin.io.path.*
import kotlin.jvm.optionals.getOrNull

internal class MinigameSerializer(
    private val minigame: Minigame
) {
    private val handler = CoroutineExceptionHandler { _, throwable ->
        ArcadeUtils.logger.error("Failed to write minigame ${this.minigame.id} data", throwable)
    }

    private val dispatcher = Dispatchers.IO.limitedParallelism(1, "MinigameSerializer")

    internal var loading: Boolean = false
        private set

    internal fun loadFrom(minigame: SerializableMinigame, path: Path) {
        val directory = directory(path)
        this.restoreComponents(directory)

        ArcadeUtils.scopedProblemReporter { reporter ->
            val files = this.dataFiles(minigame)
            val contents = this.read(directory, files, reporter)

            this.loading = true
            try {
                this.load(files, contents, Stage.BeforeInitialize)

                this.restore(contents)

                this.load(files, contents, Stage.AfterInitialize)

                if (this.minigame.initialized) {
                    this.minigame.broadcastLoadEvent()
                }

                this.load(files, contents, Stage.AfterLoad)
            } finally {
                this.loading = false
            }
            this.warnOrphanedComponents(directory, files)
        }
    }

    internal fun saveTo(minigame: SerializableMinigame, path: Path): Job {
        if (this.minigame.closed) {
            throw MinigameSerializationException("Cannot save minigame ${this.minigame.id}, it is closed")
        }

        val encoded = LinkedHashMap<String, JsonElement>()
        ArcadeUtils.scopedProblemReporter { reporter ->
            try {
                encoded["$FACTORY.json"] = this.encode(reporter, VERSION) { this.writeFactory(minigame, it) }
            } catch (e: Exception) {
                throw MinigameSerializationException("Failed to serialize minigame ${this.minigame.id} factory", e)
            }

            for (file in this.dataFiles(minigame)) {
                try {
                    encoded["${file.name}.json"] = this.encode(reporter, file.version, file.save)
                } catch (e: Exception) {
                    ArcadeUtils.logger.error("Failed to serialize '${file.name}' for minigame ${this.minigame.id}", e)
                    continue
                }
            }
        }

        val directory = directory(path)
        return this.submit {
            try {
                this.write(directory, encoded)
            } catch (e: Exception) {
                throw MinigameSerializationException("Failed to write minigame ${this.minigame.id} to $directory", e)
            }
        }
    }

    // We need to guarantee that writes happen in order
    // the [action] must not be suspending otherwise another
    // write could start before the previous one finishes
    internal fun submit(action: () -> Unit): Job {
        return scope.launch(this.dispatcher + this.handler) {
            action.invoke()
        }
    }

    private fun restore(contents: Map<String, FileContents>) {
        when (val state = this.readState(contents[MINIGAME])) {
            null, MinigameState.Created -> { }
            MinigameState.Ready -> this.minigame.tryRestore()
            is MinigameState.Playing -> {
                this.minigame.tryRestore()
                this.minigame.phases.restore(state.phase)
            }
            is MinigameState.Closed -> throw MinigameSerializationException(
                "Minigame ${this.minigame.id} was saved in a closed state"
            )
        }
    }

    private fun dataFiles(minigame: SerializableMinigame): List<DataFile> {
        val files = ArrayList<DataFile>()
        files.add(this.dataFile(MINIGAME, this::writeMinigame, this::readMinigame))
        files.add(this.dataFile("levels", this.minigame.levels::serialize, this.minigame.levels::deserialize))
        files.add(this.dataFile("tickrate", this::writeTickrate, this::readTickrate))
        files.add(this.dataFile("players", this::writePlayers, this::readPlayers))
        files.add(this.dataFile("chat", this.minigame.chat::serialize, this.minigame.chat::deserialize))
        files.add(this.dataFile("stats", this.minigame.stats::serialize, this.minigame.stats::deserialize))
        files.add(this.listedDataFile("settings", this.minigame.settings::serialize, this.minigame.settings::deserialize))
        files.add(this.listedDataFile("tags", this.minigame.tags::serialize, this.minigame.tags::deserialize))
        files.add(this.listedDataFile("recipes", this.minigame.recipes::serialize, this.minigame.recipes::deserialize, Stage.AfterLoad))
        files.add(this.listedDataFile("advancements", this.minigame.advancements::serialize, this.minigame.advancements::deserialize, Stage.AfterLoad))

        files.addAll(this.componentDataFiles())

        files.add(DataFile("custom", minigame.serializationVersion, Stage.BeforeInitialize, minigame::serialize, minigame::deserialize))
        files.add(this.dataFile("tasks", this::writeTasks, this::readTasks, Stage.AfterInitialize))
        return files
    }

    private fun restoreComponents(directory: Path) {
        val components = directory.resolve(COMPONENTS)
        if (!components.isDirectory()) {
            return
        }
        for (file in components.walk()) {
            val id = this.componentId(components, file) ?: continue
            if (this.minigame.components.has(id)) {
                continue
            }

            val factory = MinigameRegistries.MINIGAME_COMPONENT_FACTORY.getOptional(id).getOrNull() ?: continue
            val component = try {
                factory.create(this.minigame)
            } catch (e: Exception) {
                ArcadeUtils.logger.error("Failed to restore component $id for minigame ${this.minigame.id}", e)
                continue
            }
            check(component.type().id == id) { "MinigameComponentFactory $id created component ${component.type().id}!?" }
            this.minigame.components.add(component)
        }
    }

    private fun componentDataFiles(): List<DataFile> {
        val files = ArrayList<DataFile>()
        for (component in this.minigame.components.all()) {
            if (component is SerializableMinigameComponent) {
                val name = componentName(component.type().id)
                files.add(DataFile(name, component.serializationVersion, Stage.BeforeInitialize, component::serialize, component::deserialize))
            }
        }
        return files
    }

    private fun dataFile(
        name: String,
        save: (ValueOutput) -> Unit,
        load: (ValueInput) -> Unit,
        stage: Stage = Stage.BeforeInitialize
    ): DataFile {
        return DataFile(name, VERSION, stage, save) { input, _ -> load.invoke(input) }
    }

    private fun listedDataFile(
        name: String,
        save: (ValueOutput.ValueOutputList) -> Unit,
        load: (ValueInput.ValueInputList) -> Unit,
        stage: Stage = Stage.BeforeInitialize
    ): DataFile {
        return this.dataFile(
            name,
            { output -> save.invoke(output.childrenList(name)) },
            { input -> load.invoke(input.childrenListOrEmpty(name)) },
            stage
        )
    }

    private fun load(files: List<DataFile>, contents: Map<String, FileContents>, stage: Stage) {
        for (file in files) {
            if (file.stage != stage) {
                continue
            }
            val loaded = contents[file.name] ?: continue
            file.load.invoke(loaded.input, loaded.version)
        }
    }

    private fun read(directory: Path, files: List<DataFile>, reporter: ProblemReporter): Map<String, FileContents> {
        val contents = LinkedHashMap<String, FileContents>()
        for (data in files) {
            val file = directory.resolve("${data.name}.json")
            if (!file.isRegularFile()) {
                continue
            }
            val json = try {
                JsonUtils.decodeRaw<JsonObject>(file)
            } catch (e: Exception) {
                throw MinigameSerializationException("Failed to read minigame data file $file", e)
            }

            val root = JsonValueInput.create(reporter, this.minigame.server.registryAccess(), json)
            contents[data.name] = FileContents(root.childOrEmpty(DATA_KEY), root.getIntOr(VERSION_KEY, -1))
        }
        return contents
    }

    private fun warnOrphanedComponents(directory: Path, files: List<DataFile>) {
        val components = directory.resolve(COMPONENTS)
        if (!components.isDirectory()) {
            return
        }

        val known = files.mapTo(HashSet()) { "${it.name}.json" }
        for (file in components.walk()) {
            if (file.extension != "json") {
                continue
            }
            val name = directory.relativize(file).invariantSeparatorsPathString
            if (!known.contains(name)) {
                ArcadeUtils.logger.warn("Minigame ${this.minigame.id} has component data at $file which was not restored!")
            }
        }
    }

    private fun writeFactory(minigame: SerializableMinigame, output: ValueOutput) {
        output.store(FACTORY, MinigameFactory.CODEC, minigame.factory())
        output.store("uuid", UUIDUtil.STRING_CODEC, this.minigame.uuid)
    }

    private fun writeMinigame(output: ValueOutput) {
        output.store(STATE, this.stateCodec(), this.minigame.state)
        output.storeNullable("pending_phase", this.minigame.phases.codec, this.minigame.phases.pending)
        output.putInt("uptime", this.minigame.uptime)
        output.putBoolean("paused", this.minigame.paused)
    }

    private fun readMinigame(input: ValueInput) {
        this.minigame.phases.pending = input.read("pending_phase", this.minigame.phases.codec).getOrNull()
        this.minigame.uptime = input.getIntOr("uptime", 0)
        this.minigame.paused = input.getBooleanOr("paused", false)
    }

    private fun readState(contents: FileContents?): MinigameState? {
        val input = contents?.input ?: return null
        return input.read(STATE, this.stateCodec()).orElseThrow {
            MinigameSerializationException("Minigame ${this.minigame.id} was saved in an unknown state")
        }
    }

    private fun stateCodec(): Codec<MinigameState> {
        return MinigameState.codec(this.minigame.phases.codec)
    }

    private fun writeTickrate(output: ValueOutput) {
        val tickrate = this.minigame.tickrate
        output.putBoolean("use_global_tickrate", tickrate.useGlobalManager)
        output.putBoolean("frozen", tickrate.isFrozen)
        output.putFloat("tickrate", tickrate.tickrate())
    }

    private fun readTickrate(input: ValueInput) {
        val tickrate = this.minigame.tickrate
        tickrate.useGlobalManager = input.getBooleanOr("use_global_tickrate", tickrate.useGlobalManager)
        tickrate.isFrozen = input.getBooleanOr("frozen", false)
        tickrate.setTickRate(input.getFloatOr("tickrate", tickrate.tickrate()))
    }

    private fun writePlayers(output: ValueOutput) {
        this.minigame.teams.serialize(output.child("teams"))

        output.store("players", NameAndId.CODEC.listOf(), this.minigame.players.allProfiles)
        output.store("spectators", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.spectatorUUIDs)
        output.store("admins", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.adminUUIDs)
    }

    private fun readPlayers(input: ValueInput) {
        this.minigame.teams.deserialize(input.childOrEmpty("teams"), this.minigame.server.scoreboard)

        this.minigame.players.offlineGameProfiles.addAll(input.listOrEmpty("players", NameAndId.CODEC))
        this.minigame.players.spectatorUUIDs.addAll(input.listOrEmpty("spectators", UUIDUtil.STRING_CODEC))
        this.minigame.players.adminUUIDs.addAll(input.listOrEmpty("admins", UUIDUtil.STRING_CODEC))
    }

    private fun writeTasks(output: ValueOutput) {
        this.minigame.scopes.serialize(output.childrenList("scheduled_tasks"))
    }

    private fun readTasks(input: ValueInput) {
        this.minigame.scopes.deserialize(input.childrenListOrEmpty("scheduled_tasks"))
    }

    private fun encode(reporter: ProblemReporter, version: Int, save: (ValueOutput) -> Unit): JsonElement {
        val output = JsonValueOutput.create(reporter, this.minigame.server.registryAccess())
        output.putInt(VERSION_KEY, version)
        save.invoke(output.child(DATA_KEY))
        return output.buildResult()
    }

    private fun write(directory: Path, files: Map<String, JsonElement>) {
        for ((name, json) in files) {
            val file = directory.resolve(name)
            file.createParentDirectories()

            val temporary = file.resolveSibling("${file.name}.tmp")
            JsonUtils.encodeRaw(json, temporary)
            replace(temporary, file)
        }
    }

    private fun replace(from: Path, to: Path) {
        try {
            from.moveTo(to, StandardCopyOption.ATOMIC_MOVE)
        } catch (_: AtomicMoveNotSupportedException) {
            from.moveTo(to, overwrite = true)
        }
    }

    private fun componentName(id: Identifier): String {
        return "$COMPONENTS/${id.namespace}/${id.path}"
    }

    private fun componentId(components: Path, file: Path): Identifier? {
        if (file.extension != "json") {
            return null
        }
        // A namespace cannot contain a slash, but a path can
        val name = components.relativize(file).invariantSeparatorsPathString.removeSuffix(".json")
        return Identifier.tryBySeparator(name, '/')
    }

    private class DataFile(
        val name: String,
        val version: Int,
        val stage: Stage,
        val save: (ValueOutput) -> Unit,
        val load: (ValueInput, Int) -> Unit
    )

    private class FileContents(
        val input: ValueInput,
        val version: Int
    )

    private enum class Stage {
        BeforeInitialize,
        AfterInitialize,
        AfterLoad
    }

    internal companion object {
        private const val VERSION: Int = 0

        private const val VERSION_KEY = "version"
        private const val DATA_KEY = "data"

        private const val COMPONENTS = "components"

        private const val FACTORY = "factory"
        private const val MINIGAME = "minigame"

        private const val STATE = "state"

        private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

        internal fun awaitPending() {
            runBlocking {
                scope.coroutineContext.job.children.toList().joinAll()
            }
        }

        internal fun directory(path: Path): Path {
            return path.resolve("data")
        }

        internal fun createFrom(path: Path, server: MinecraftServer): Minigame {
            val file = directory(path).resolve("$FACTORY.json")
            if (!file.isRegularFile()) {
                throw MinigameCreationException("Cannot create minigame, no such file $file")
            }
            val json = try {
                JsonUtils.decodeRaw<JsonObject>(file)
            } catch (e: Exception) {
                throw MinigameCreationException("Cannot create minigame, failed to read $file", e)
            }

            ArcadeUtils.scopedProblemReporter { reporter ->
                val root = JsonValueInput.create(reporter, server.registryAccess(), json)
                val input = root.childOrEmpty(DATA_KEY)
                try {
                    val factory = input.read(FACTORY, MinigameFactory.CODEC).orElseThrow {
                        MinigameCreationException("Failed to decode minigame factory")
                    }
                    val uuid = input.read("uuid", UUIDUtil.STRING_CODEC).orElseThrow()
                    val existing = Minigames.get(uuid)
                    if (existing != null) {
                        throw MinigameCreationException(
                            "Cannot create minigame for $path, $existing is already loaded with the same uuid"
                        )
                    }
                    val minigame = factory.create(MinigameCreationContext(server, uuid))
                    if (minigame !is SerializableMinigame) {
                        throw MinigameCreationException(
                            "Minigame ${minigame.id} doesn't support loading, was this refactored?"
                        )
                    }
                    minigame.serializer.loadFrom(minigame, path)
                    return minigame
                } catch (exception: MinigameCreationException) {
                    throw exception
                } catch (exception: Exception) {
                    throw MinigameCreationException("Failed to create minigame for $path", exception)
                }
            }
        }
    }
}
