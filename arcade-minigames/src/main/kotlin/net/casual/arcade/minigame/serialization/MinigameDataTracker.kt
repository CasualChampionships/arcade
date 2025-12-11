/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.longOrNull
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.uuid
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull
import kotlin.time.*

// TODO: What the hell is this class, why...?
@OptIn(ExperimentalTime::class)
public class MinigameDataTracker(
    private val minigame: Minigame
) {
    private val players = ConcurrentHashMap<UUID, JsonObject>()
    public var startTime: Instant = Instant.DISTANT_PAST
        private set
    public var endTime: Instant = Instant.DISTANT_FUTURE
        private set

    public fun start() {
        if (this.startTime == Instant.DISTANT_PAST) {
            this.startTime = Clock.System.now()
        }
    }

    public fun end() {
        if (this.endTime != Instant.DISTANT_FUTURE) {
            return
        }

        for (player in this.minigame.players) {
            this.updatePlayer(player)
        }

        this.endTime = Clock.System.now()
    }

    public fun updatePlayer(player: ServerPlayer) {
        if (this.endTime != Instant.DISTANT_FUTURE) {
            return
        }

        val json = JsonObject()
        json.addProperty("uuid", player.stringUUID)
        // json.add("stats", this.minigame.stats.serialize(player))
        val advancements = JsonArray()
        for (advancement in this.minigame.advancements.all()) {
            if (!player.advancements.getOrStartProgress(advancement).isDone) {
                continue
            }
            val display = advancement.value.display.getOrNull() ?: continue
            val data = JsonObject()
            data.addProperty("id", advancement.id.toString())
            data.addProperty("item", BuiltInRegistries.ITEM.getKey(display.icon.item).toString())
            data.addProperty("title", display.title.string)
            advancements.add(data)
        }
        json.add("advancements", advancements)

        this.players[player.uuid] = json
    }

    public fun getAdvancements(uuid: UUID): List<AdvancementHolder> {
        val json = this.players[uuid] ?: return listOf()
        val list = ArrayList<AdvancementHolder>()
        for (data in json.array("advancements").objects()) {
            val id = Identifier.parse(data.string("id"))
            list.add(this.minigame.advancements.get(id) ?: continue)
        }
        return list
    }

    internal fun serialize(modifier: ((UUID, JsonObject) -> Unit)? = null): JsonObject {
        for (player in this.minigame.players) {
            this.updatePlayer(player)
        }

        val json = JsonObject()
        if (!this.startTime.isDistantPast) {
            json.addProperty("minigame_start_ms", this.startTime.toEpochMilliseconds())
        }
        if (!this.endTime.isDistantFuture) {
            json.addProperty("minigame_end_ms", this.endTime.toEpochMilliseconds())
        }
        json.addProperty("id", this.minigame.id.toString())
        json.addProperty("uuid", this.minigame.uuid.toString())
        val players = JsonArray()
        for ((uuid, data) in this.players) {
            val player = if (modifier != null) { data.deepCopy().also { modifier(uuid, it) } } else data
            players.add(player)
        }
        json.add("players", players)
        return json
    }

    internal fun deserialize(json: JsonObject) {
        val startMs = json.longOrNull("minigame_start_ms")
        if (startMs != null) {
            this.startTime = Instant.fromEpochMilliseconds(startMs)
        }
        val endMs = json.longOrNull("minigame_end_ms")
        if (endMs != null) {
            this.endTime = Instant.fromEpochMilliseconds(endMs)
        }

        for (player in json.array("players").objects()) {
            this.players[player.uuid("uuid")] = player
        }
    }
}