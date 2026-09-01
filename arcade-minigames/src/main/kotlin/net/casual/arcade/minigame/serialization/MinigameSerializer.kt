/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameState
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.serialization.codec.setOf
import net.casual.arcade.utils.serialization.json.JsonValueInput
import net.casual.arcade.utils.serialization.json.JsonValueOutput
import net.minecraft.core.UUIDUtil
import net.minecraft.server.players.NameAndId
import net.minecraft.util.Util
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.Internal
import java.io.*
import java.nio.file.Path
import kotlin.io.path.isRegularFile

@Internal
public class MinigameSerializer(
    private val minigame: Minigame
) {
    internal fun loadFrom(path: Path) {
        this.readAsObjectFrom(path.resolve("tasks.json"), this::readTasksJson)
        this.readAsObjectFrom(path.resolve("players.json"), this::readPlayersJson)
        this.readAsObjectFrom(path.resolve("chat_manager.json"), this.minigame.chat::deserialize)
        this.readAsListFrom(path.resolve("settings.json"), this.minigame.settings::deserialize)
        this.readAsListFrom(path.resolve("stats.json"), this.minigame.stats::deserialize)
        this.readAsListFrom(path.resolve("tags.json"), this.minigame.tags::deserialize)
        this.readAsListFrom(path.resolve("recipes.json"), this.minigame.recipes::deserialize)
        this.readAsListFrom(path.resolve("advancements.json"), this.minigame.advancements::deserialize)
        this.readAsObjectFrom(path.resolve("components.json"), this.minigame.components::deserialize)
        this.readAsObjectFrom(path.resolve("custom.json"), this.minigame::internalLoad)
        this.readAsObjectFrom(path.resolve("minigame.json"), this::readMinigame)
    }

    internal fun saveTo(path: Path) {
        this.writeAsyncAsObjectInto(path.resolve("tasks.json"), this::writeTasksJson)
        this.writeAsyncAsObjectInto(path.resolve("players.json"), this::writePlayerJson)
        this.writeAsyncAsObjectInto(path.resolve("chat_manager.json"), this.minigame.chat::serialize)
        this.writeAsyncAsListInto(path.resolve("settings.json"), this.minigame.settings::serialize)
        this.writeAsyncAsListInto(path.resolve("stats.json"), this.minigame.stats::serialize)
        this.writeAsyncAsListInto(path.resolve("tags.json"), this.minigame.tags::serialize)
        this.writeAsyncAsListInto(path.resolve("recipes.json"), this.minigame.recipes::serialize)
        this.writeAsyncAsListInto(path.resolve("advancements.json"), this.minigame.advancements::serialize)
        this.writeAsyncAsObjectInto(path.resolve("components.json"), this.minigame.components::serialize)
        this.writeAsyncAsObjectInto(path.resolve("custom.json"), this.minigame::internalSave)

        this.writeAsyncAsObjectInto(path.resolve("minigame.json"), this::writeMinigame)
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

    private fun readMinigame(input: ValueInput) {
        // FIXME: Update this when we migrate phase serialization stuff
        val state = input.getStringOr("state", CREATED)

        this.minigame.uptime = input.getIntOr("uptime", 0)
        this.minigame.paused = input.getBooleanOr("paused", false)

        val tickrate = this.minigame.tickrate
        tickrate.useGlobalManager = input.getBooleanOr("use_global_tickrate", tickrate.useGlobalManager)
        tickrate.isFrozen = input.getBooleanOr("frozen", false)
        tickrate.setTickRate(input.getFloatOr("tickrate", tickrate.tickrate()))

        if (state == READY || state == PLAYING) {
            this.minigame.tryInitialize()
        }
        if (state == PLAYING) {
            val phase = input.read("phase", this.minigame.phases.codec).orElseThrow {
                IllegalStateException("Minigame phase is invalid, unable to deserialize minigame")
            }
            this.minigame.phases.restore(phase)
        }
    }

    private fun readTasksJson(input: ValueInput) {
        this.minigame.scopes.deserialize(input.childrenListOrEmpty("scheduled_tasks"))
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

    private fun writeMinigame(output: ValueOutput) {
        when (val state = this.minigame.state) {
            MinigameState.Created -> output.putString("state", CREATED)
            MinigameState.Ready -> output.putString("state", READY)
            is MinigameState.Playing -> {
                output.putString("state", PLAYING)
                output.store("phase", this.minigame.phases.codec, state.phase)
            }
            is MinigameState.Closed -> output.putString("state", CREATED)
        }
        output.putInt("uptime", this.minigame.uptime)
        output.putBoolean("paused", this.minigame.paused)

        val tickrate = this.minigame.tickrate
        output.putBoolean("use_global_tickrate", tickrate.useGlobalManager)
        output.putBoolean("frozen", tickrate.isFrozen)
        output.putFloat("tickrate", tickrate.tickrate())
    }

    private companion object {
        private const val CREATED = "created"
        private const val READY = "ready"
        private const val PLAYING = "playing"
    }

    private fun writeTasksJson(output: ValueOutput) {
        this.minigame.scopes.serialize(output.childrenList("scheduled_tasks"))
    }

    private fun writePlayerJson(output: ValueOutput) {
        this.minigame.teams.serialize(output.child("teams"))

        output.store("players", NameAndId.CODEC.listOf(), this.minigame.players.allProfiles)
        output.store("spectators", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.spectatorUUIDs)
        output.store("admins", UUIDUtil.STRING_CODEC.setOf(), this.minigame.players.adminUUIDs)
    }
}
