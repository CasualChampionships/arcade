/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import kotlinx.atomicfu.atomic
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.task.MinigameTaskCreationContext
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.scheduler.task.utils.TaskRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.intOrDefault
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.serialization.EmptyValueInput
import net.casual.arcade.utils.serialization.json.JsonValueInput
import net.casual.arcade.utils.serialization.json.JsonValueOutput
import net.casual.arcade.utils.setOf
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import net.minecraft.server.players.NameAndId
import net.minecraft.util.Util
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.*
import java.nio.file.Path
import kotlin.io.encoding.Base64
import kotlin.io.path.isRegularFile
import kotlin.jvm.optionals.getOrNull
import kotlin.toString

@Internal
public class MinigameSerializer(
    private val minigame: Minigame
) {
    internal fun loadFrom(path: Path) {
        this.readAsObjectFrom(path.resolve("tasks.json"), this::readTasksJson)
        this.readAsObjectFrom(path.resolve("players.json"), this::readPlayersJson)
        this.readAsObjectFrom(path.resolve("chat_manager.json"), this.minigame.chat::deserialize)
        this.readAsJsonArrayFrom(path.resolve("settings.json"), this.minigame.settings::deserialize)
        this.readAsListFrom(path.resolve("stats.json"), this.minigame.stats::deserialize)
        this.readAsJsonArrayFrom(path.resolve("tags.json"), this.minigame.tags::deserialize)
        this.readAsJsonArrayFrom(path.resolve("recipes.json"), this.minigame.recipes::deserialize)
        this.readAsJsonObjectFrom(path.resolve("data_tracker.json"), this.minigame.data::deserialize)
        this.readAsJsonObjectFrom(path.resolve("custom.json"), this.minigame::internalLoad)
        this.readAsJsonObjectFrom(path.resolve("minigame.json"), this::readMinigameJson)
    }

    internal fun saveTo(path: Path) {
        this.writeAsyncAsObjectInto(path.resolve("tasks.json"), this::writeTasksJson)
        this.writeAsyncAsObjectInto(path.resolve("players.json"), this::writePlayerJson)
        this.writeAsyncAsObjectInto(path.resolve("chat_manager.json"), this.minigame.chat::serialize)
        this.writeAsyncAsListInto(path.resolve("settings.json"), this.minigame.settings::serialize)
        this.writeAsyncAsListInto(path.resolve("stats.json"), this.minigame.stats::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("tags.json"), this.minigame.tags::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("recipes.json"), this.minigame.recipes::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("data_tracker.json"), this.minigame.data::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("custom.json"), this.minigame::internalSave)

        this.writeAsyncAsJsonElementInto(path.resolve("minigame.json"), this::writeMinigameJson)
    }

    private inline fun readAsJsonObjectFrom(path: Path, block: (JsonObject) -> Unit) {
        if (path.isRegularFile()) {
            block.invoke(JsonUtils.decodeRaw(path))
        }
    }

    private inline fun readAsJsonArrayFrom(path: Path, block: (JsonArray) -> Unit) {
        if (path.isRegularFile()) {
            block.invoke(JsonUtils.decodeRaw(path))
        }
    }

    private inline fun readAsObjectFrom(path: Path, crossinline block: (ValueInput) -> Unit) {
        this.readAsJsonObjectFrom(path) { json: JsonObject ->
            ArcadeUtils.scopedProblemReporter { reporter ->
                block.invoke(JsonValueInput.create(reporter, this.minigame.server.registryAccess(), json))
            }
        }
    }

    private inline fun readAsListFrom(path: Path, crossinline block: (ValueInput.ValueInputList) -> Unit) {
        this.readAsJsonArrayFrom(path) { json: JsonArray ->
            ArcadeUtils.scopedProblemReporter { reporter ->
                block.invoke(JsonValueInput.create(reporter, this.minigame.server.registryAccess(), json))
            }
        }
    }

    private fun readMinigameJson(json: JsonObject) {
        val initialized = json.booleanOrDefault("initialized")
        this.minigame.started = json.booleanOrDefault("started")

        val phaseId = json.string("phase")
        this.minigame.phase = requireNotNull(this.minigame.getPhase(phaseId)) {
            "Minigame phase $phaseId is invalid, unable to deserialize minigame"
        }

        this.minigame.uptime = json.intOrDefault("uptime")
        this.minigame.paused = json.booleanOrDefault("paused")
        this.minigame.tickrate.isFrozen = json.booleanOrDefault("frozen")

        if (initialized) {
            this.minigame.tryInitialize()
        }
        for (phase in this.minigame.phases) {
            if (phase <= this.minigame.phase) {
                phase.initialize(this.minigame)
            }
        }
    }

    private fun readTasksJson(input: ValueInput) {
        val context = MinigameTaskCreationContextImpl(this.minigame)
        context.deserialize(input.childrenListOrEmpty("task_definitions"))
        this.minigame.scheduler.minigame.deserialize(input.childrenListOrEmpty("scheduled_tasks"), context)
        this.minigame.scheduler.phased.deserialize(input.childrenListOrEmpty("scheduled_phase_tasks"), context)
        context.clear()
    }

    private fun readPlayersJson(input: ValueInput) {
        this.minigame.teams.deserialize(input.childOrEmpty("teams"), this.minigame.server.scoreboard)

        this.minigame.players.offlineGameProfiles.addAll(input.listOrEmpty("players", NameAndId.CODEC))
        this.minigame.players.spectatorUUIDs.addAll(input.listOrEmpty("spectators", UUIDUtil.STRING_CODEC))
        this.minigame.players.adminUUIDs.addAll(input.listOrEmpty("admins", UUIDUtil.STRING_CODEC))
    }

    private inline fun writeAsyncAsJsonElementInto(path: Path, block: () -> JsonElement) {
        val json = block.invoke()
        Util.ioPool().execute {
            try {
                JsonUtils.encodeRaw(json, path)
            } catch (e: IOException) {
                ArcadeUtils.logger.error("Failed to write minigame data to $path", e)
            }
        }
    }

    private inline fun writeAsyncAsObjectInto(path: Path, block: (ValueOutput) -> Unit) {
        this.writeAsyncAsJsonElementInto(path) {
            ArcadeUtils.createProblemReporter().use { reporter ->
                val output = JsonValueOutput.create(reporter, this.minigame.server.registryAccess())
                block.invoke(output)
                output.buildResult()
            }
        }
    }

    private inline fun writeAsyncAsListInto(path: Path, block: (ValueOutput.ValueOutputList) -> Unit) {
        this.writeAsyncAsJsonElementInto(path) {
            ArcadeUtils.createProblemReporter().use { reporter ->
                val output = JsonValueOutput.createList(reporter, this.minigame.server.registryAccess())
                block.invoke(output)
                output.buildResult()
            }
        }
    }

    private fun writeMinigameJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("initialized", this.minigame.initialized)
        json.addProperty("started", this.minigame.started)
        json.addProperty("phase", this.minigame.phase.id)
        json.addProperty("uptime", this.minigame.uptime)
        json.addProperty("paused", this.minigame.paused)
        json.addProperty("frozen", this.minigame.tickrate.isFrozen)
        return json
    }

    private fun writeTasksJson(output: ValueOutput) {
        val context = MinigameTaskSerializationContextImpl()
        this.minigame.scheduler.minigame.serialize(output.childrenList("scheduled_tasks"), context)
        this.minigame.scheduler.phased.serialize(output.childrenList("scheduled_phase_tasks"), context)
        context.serialize(output.childrenList("task_definitions"))
        context.clear()
    }

    private fun writePlayerJson(output: ValueOutput) {
        this.minigame.teams.serialize(output.child("teams"))

        output.store("players", NameAndId.CODEC.listOf(), this.minigame.players.allProfiles)
        output.store("spectators", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.spectatorUUIDs)
        output.store("admins", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.adminUUIDs)
    }

    private class MinigameTaskCreationContextImpl(
        override val minigame: Minigame
    ): MinigameTaskCreationContext<Minigame> {
        private val generated = Int2ObjectOpenHashMap<Task>()

        override fun getTask(uid: Int): Task? {
            if (this.generated.containsKey(uid)) {
                return this.generated.get(uid)
            }
            return null
        }

        fun clear() {
            this.generated.clear()
        }

        fun deserialize(list: ValueInput.ValueInputList) {
            for (input in list) {
                val id = input.getInt("uid").getOrNull() ?: continue
                val task = if (input.contains("raw")) this.deserializeRaw(input) else this.deserializeSavable(input)
                if (task == null) {
                    val meta = input.getStringOr("meta", "Missing metadata")
                    ArcadeUtils.logger.error("Failed to deserialize task $id, meta: $meta")
                    continue
                }
                this.generated.put(id, task)
            }
        }

        private fun deserializeRaw(input: ValueInput): Task? {
            return try {
                Base64.decode(input.getString("raw").get()).inputStream().use { bytes ->
                    ObjectInputStream(bytes).use { it.readObject() as Task }
                }
            } catch (_: ObjectStreamException) {
                null
            }
        }

        private fun deserializeSavable(input: ValueInput): Task? {
            val id = input.read("id", Identifier.CODEC).getOrNull() ?: return null
            val custom = input.childOrEmpty("custom")
            val factory = TaskRegistries.TASK_FACTORY.getOptional(id).getOrNull() ?: return null
            return factory.create(custom, this)
        }
    }

    private class MinigameTaskSerializationContextImpl: TaskSerializationContext {
        private val ids = Reference2IntOpenHashMap<Task>()
        private val id = atomic(0)

        override fun storeTask(task: Task): Int {
            if (this.ids.containsKey(task)) {
                return this.ids.getInt(task)
            }
            val next = this.id.getAndIncrement()
            this.ids.put(task, next)
            return next
        }

        fun clear() {
            this.ids.clear()
        }

        fun serialize(output: ValueOutput.ValueOutputList) {
            for (entry in this.ids.reference2IntEntrySet()) {
                val task = entry.key
                val id = entry.intValue
                val child = output.addChild()
                val success = when (task) {
                    is SavableTask -> this.serializeSavable(child, id, task)
                    is Serializable -> this.serializeRaw(child, id, task)
                    else -> false
                }
                if (success) {
                    child.putString("meta", "${task::class.java.simpleName}: $task")
                } else {
                    output.discardLast()
                }
            }
        }

        private fun serializeSavable(output: ValueOutput, uid: Int, task: SavableTask): Boolean {
            try {
                output.store("id", Identifier.CODEC, task.id)
                output.putInt("uid", uid)
                task.serialize(output.child("custom"), this)
                return true
            } catch (e: Exception) {
                ArcadeUtils.logger.error("Failed to serialize task ${task.id}", e)
            }
            return false
        }

        private fun serializeRaw(output: ValueOutput, uid: Int, task: Serializable): Boolean {
            try {
                ByteArrayOutputStream().use { bytes ->
                    ObjectOutputStream(bytes).use { stream ->
                        stream.writeObject(task)
                    }
                    output.putInt("uid", uid)
                    output.putString("raw", Base64.encode(bytes.toByteArray()))
                }
                return true
            } catch (_: ObjectStreamException) {

            }
            return false
        }
    }
}