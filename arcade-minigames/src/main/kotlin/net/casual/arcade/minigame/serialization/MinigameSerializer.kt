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
import net.minecraft.Util
import net.minecraft.resources.ResourceLocation
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.*
import java.nio.file.Path
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.io.path.isRegularFile
import kotlin.io.path.reader
import kotlin.io.path.writer
import kotlin.jvm.optionals.getOrNull

@Internal
public class MinigameSerializer(
    private val minigame: Minigame
) {
    internal fun loadFrom(path: Path) {
        this.readAsJsonObjectInto(path.resolve("tasks.json"), this::readTasksJson)
        this.readAsJsonObjectInto(path.resolve("players.json"), this::readPlayersJson)
        this.readAsJsonObjectInto(path.resolve("chat_manager.json"), this.minigame.chat::deserialize)
        this.readAsJsonArrayInto(path.resolve("settings.json"), this.minigame.settings::deserialize)
        this.readAsJsonArrayInto(path.resolve("stats.json"), this.minigame.stats::deserialize)
        this.readAsJsonArrayInto(path.resolve("tags.json"), this.minigame.tags::deserialize)
        this.readAsJsonArrayInto(path.resolve("recipes.json"), this.minigame.recipes::deserialize)
        this.readAsJsonObjectInto(path.resolve("data_tracker.json"), this.minigame.data::deserialize)
        this.readAsJsonObjectInto(path.resolve("custom.json"), this.minigame::internalLoad)
        this.readAsJsonObjectInto(path.resolve("minigame.json"), this::readMinigameJson)
    }

    internal fun saveTo(path: Path) {
        this.writeAsyncAsJsonElementInto(path.resolve("tasks.json"), this::writeTasksJson)
        this.writeAsyncAsJsonElementInto(path.resolve("players.json"), this::writePlayerJson)
        this.writeAsyncAsJsonElementInto(path.resolve("chat_manager.json"), this.minigame.chat::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("settings.json"), this.minigame.settings::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("stats.json"), this.minigame.stats::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("tags.json"), this.minigame.tags::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("recipes.json"), this.minigame.recipes::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("data_tracker.json"), this.minigame.data::serialize)
        this.writeAsyncAsJsonElementInto(path.resolve("custom.json"), this.minigame::internalSave)

        this.writeAsyncAsJsonElementInto(path.resolve("minigame.json"), this::writeMinigameJson)
    }

    private inline fun readAsJsonObjectInto(path: Path, block: (JsonObject) -> Unit) {
        if (path.isRegularFile()) {
            block.invoke(path.reader().use(JsonUtils::decodeToJsonObject))
        }
    }

    private inline fun readAsJsonArrayInto(path: Path, block: (JsonArray) -> Unit) {
        if (path.isRegularFile()) {
            block.invoke(path.reader().use(JsonUtils::decodeToJsonArray))
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

    private fun readPlayersJson(json: JsonObject) {
        this.minigame.teams.deserialize(json.objOrDefault("teams"), this.minigame.server.scoreboard)

        for (player in json.arrayOrDefault("players").objects()) {
            val profile = GameProfile(player.uuidOrNull("uuid"), player.stringOrNull("name"))
            this.minigame.players.offlineGameProfiles.add(profile)
        }

        this.minigame.players.spectatorUUIDs.addAll(json.arrayOrDefault("spectators").uuids())
        this.minigame.players.adminUUIDs.addAll(json.arrayOrDefault("admins").uuids())
    }

    @OptIn(ExperimentalEncodingApi::class)
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
            val factory = TaskRegistries.TASK_FACTORY.getOptional(ResourceLocation.parse(id)).getOrNull() ?: return null
            factory.create(context.createSubContext(custom))
        }
        context.generated.put(identity, task)
        return task
    }

    private inline fun writeAsyncAsJsonElementInto(path: Path, block: () -> JsonElement) {
        val json = block.invoke()
        Util.ioPool().execute {
            try {
                path.writer().use {
                    JsonUtils.encode(json, it)
                }
            } catch (e: IOException) {
                ArcadeUtils.logger.error("Failed to write minigame data to $path", e)
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

    private fun writePlayerJson(): JsonObject {
        val json = JsonObject()
        val players = JsonArray()
        for (player in this.minigame.players.allProfiles) {
            val data = JsonObject()
            data.addProperty("name", player.name)
            data.addProperty("uuid", player.id?.toString())
            players.add(data)
        }

        val spectators = JsonArray()
        for (spectator in this.minigame.players.spectatorUUIDs) {
            spectators.add(spectator.toString())
        }

        val admins = JsonArray()
        for (admin in this.minigame.players.adminUUIDs) {
            admins.add(admin.toString())
        }
        json.add("teams", this.minigame.teams.serialize())
        json.add("players", players)
        json.add("spectators", spectators)
        json.add("admins", admins)
        return json
    }

    @OptIn(ExperimentalEncodingApi::class)
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