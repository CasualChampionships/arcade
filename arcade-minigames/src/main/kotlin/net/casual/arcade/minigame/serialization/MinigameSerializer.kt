/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.authlib.GameProfile
import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.task.MinigameTaskCreationContext
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskSerializationContext
import net.casual.arcade.scheduler.task.utils.TaskRegistries
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.booleanOrDefault
import net.casual.arcade.utils.JsonUtils.int
import net.casual.arcade.utils.JsonUtils.intOrDefault
import net.casual.arcade.utils.JsonUtils.objOrDefault
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.casual.arcade.utils.JsonUtils.toJsonArray
import net.casual.arcade.utils.JsonUtils.uuidOrNull
import net.casual.arcade.utils.JsonUtils.uuids
import net.casual.arcade.utils.serialization.json.JsonValueInput
import net.casual.arcade.utils.serialization.json.JsonValueOutput
import net.casual.arcade.utils.setOf
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import net.minecraft.server.players.NameAndId
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.Util
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.*
import java.nio.file.Path
import kotlin.io.encoding.Base64
import kotlin.io.path.isRegularFile
import kotlin.jvm.optionals.getOrNull

@Internal
public class MinigameSerializer(
    private val minigame: Minigame
) {
    internal fun loadFrom(path: Path) {
        this.readAsJsonObjectFrom(path.resolve("tasks.json"), this::readTasksJson)
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
        this.writeAsyncAsJsonElementInto(path.resolve("tasks.json"), this::writeTasksJson)
        this.writeAsyncAsObjectInto(path.resolve("players.json"), this::writePlayerJson)
        this.writeAsyncAsObjectInto(path.resolve("chat_manager.json"), this.minigame.chat::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("settings.json"), this.minigame.settings::serialize)
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

    private fun readTasksJson(json: JsonObject) {
        val definitions = Int2ObjectOpenHashMap<JsonObject>()
        for (definition in json.arrayOrDefault("task_definitions").objects()) {
            definitions.put(definition.int("uid"), definition)
        }
        val context = MinigameTaskCreationContextImpl(JsonObject(), definitions, Int2ObjectOpenHashMap())
        this.minigame.scheduler.minigame.deserialize(json.arrayOrDefault("scheduled_tasks"), context)
        this.minigame.scheduler.phased.deserialize(json.arrayOrDefault("scheduled_phase_tasks"), context)
        context.clear()
    }

    private fun readPlayersJson(input: ValueInput) {
        this.minigame.teams.deserialize(input.childOrEmpty("teams"), this.minigame.server.scoreboard)

        this.minigame.players.offlineGameProfiles.addAll(input.listOrEmpty("players", NameAndId.CODEC))
        this.minigame.players.spectatorUUIDs.addAll(input.listOrEmpty("spectators", UUIDUtil.STRING_CODEC))
        this.minigame.players.adminUUIDs.addAll(input.listOrEmpty("admins", UUIDUtil.STRING_CODEC))
    }

    private fun deserializeTask(identity: Int, context: MinigameTaskCreationContextImpl): Task? {
        if (context.generated.containsKey(identity)) {
            return context.generated.get(identity)
        }

        val definition = context.definitions.get(identity) ?: return null
        val task = if (definition.has("raw")) {
            try {
                Base64.decode(definition.string("raw")).inputStream().use { bytes ->
                    ObjectInputStream(bytes).use { it.readObject() as Task }
                }
            } catch (_: ObjectStreamException) {
                null
            }
        } else {
            val id = definition.stringOrNull("id") ?: return null
            val custom = definition.objOrDefault("custom")
            val factory = TaskRegistries.TASK_FACTORY.getOptional(Identifier.parse(id)).getOrNull() ?: return null
            factory.create(context.createSubContext(custom))
        }
        context.generated.put(identity, task)
        return task
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

    private fun writeTasksJson(): JsonObject {
        val json = JsonObject()
        val context = MinigameTaskSerializationContext(Int2ObjectOpenHashMap())
        json.add("scheduled_tasks", this.minigame.scheduler.minigame.serialize(context))
        json.add("scheduled_phase_tasks", this.minigame.scheduler.phased.serialize(context))
        json.add("task_definitions", context.definitions.values.toJsonArray())
        context.clear()
        return json
    }

    private fun writePlayerJson(output: ValueOutput) {
        this.minigame.teams.serialize(output.child("teams"))

        output.store("players", NameAndId.CODEC.listOf(), this.minigame.players.allProfiles)
        output.store("spectators", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.spectatorUUIDs)
        output.store("admins", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.adminUUIDs)
    }

    private fun serializeTask(task: Task, context: MinigameTaskSerializationContext): Int? {
        val identity = System.identityHashCode(task)
        if (context.definitions.containsKey(identity)) {
            return identity
        }

        if (task is SavableTask) {
            try {
                val definition = JsonObject()
                definition.addProperty("id", task.id.toString())
                definition.addProperty("uid", identity)
                definition.add("custom", task.serialize(context))
                context.definitions.put(identity, definition)
                return identity
            } catch (e: Exception) {
                ArcadeUtils.logger.error("Failed to serialize task ${task.id}", e)
            }
        }

        if (task is Serializable) {
            try {
                ByteArrayOutputStream().use { bytes ->
                    ObjectOutputStream(bytes).use { stream ->
                        stream.writeObject(task)
                    }
                    val definition = JsonObject()
                    definition.addProperty("uid", identity)
                    definition.addProperty("raw", Base64.encode(bytes.toByteArray()))
                    context.definitions.put(identity, definition)
                    return identity
                }
            } catch (_: ObjectStreamException) {

            }
        }
        return null
    }

    private inner class MinigameTaskCreationContextImpl(
        override val data: JsonObject,
        val definitions: Int2ObjectMap<JsonObject>,
        val generated: Int2ObjectMap<Task?>,
    ): MinigameTaskCreationContext<Minigame> {
        override val minigame: Minigame
            get() = this@MinigameSerializer.minigame

        override fun createTask(uid: Int): Task? {
            val task = deserializeTask(uid, this)
            if (task == null) {
                ArcadeUtils.logger.warn("Saved task $uid for minigame ${this.minigame.id} could not be reloaded!")
            }
            return task
        }

        fun clear() {
            this.definitions.clear()
            this.generated.clear()
        }
    }

    private inner class MinigameTaskSerializationContext(
        val definitions: Int2ObjectMap<JsonObject>
    ): TaskSerializationContext {
        override fun serializeTask(task: Task): Int? {
            return serializeTask(task, this)
        }

        fun clear() {
            this.definitions.clear()
        }
    }
}