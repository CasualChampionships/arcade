/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.google.common.collect.HashMultimap
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddTagEvent
import net.casual.arcade.minigame.events.MinigameRemoveTagEvent
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.JsonUtils.uuid
import net.casual.arcade.utils.PlayerUtils.player
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class MinigameTagManager(
    private val minigame: Minigame
) {
    private val players = HashMultimap.create<UUID, ResourceLocation>()
    private val tags = HashMultimap.create<ResourceLocation, UUID>()

    public fun get(uuid: UUID): Set<ResourceLocation> {
        return this.players.get(uuid)
    }

    public fun get(player: ServerPlayer): Set<ResourceLocation> {
        return this.get(player.uuid)
    }

    public fun has(uuid: UUID, tag: ResourceLocation): Boolean {
        return this.players.containsEntry(uuid, tag)
    }

    public fun has(player: ServerPlayer, tag: ResourceLocation): Boolean {
        return this.has(player.uuid, tag)
    }

    public fun add(uuid: UUID, tag: ResourceLocation): Boolean {
        val success = this.players.put(uuid, tag)
        this.tags.put(tag, uuid)
        if (success) {
            val player = this.minigame.server.player(uuid)
            if (player != null) {
                GlobalEventHandler.Server.broadcast(MinigameAddTagEvent(this.minigame, player, tag))
            }
        }
        return success
    }

    public fun add(player: ServerPlayer, tag: ResourceLocation): Boolean {
        return this.add(player.uuid, tag)
    }

    public fun remove(uuid: UUID, tag: ResourceLocation): Boolean {
        val success = this.players.remove(uuid, tag)
        this.tags.remove(tag, uuid)
        if (success) {
            val player = this.minigame.server.player(uuid)
            if (player != null) {
                GlobalEventHandler.Server.broadcast(MinigameRemoveTagEvent(this.minigame, player, tag))
            }
        }
        return success
    }

    public fun remove(player: ServerPlayer, tag: ResourceLocation): Boolean {
        return this.remove(player.uuid, tag)
    }

    public fun getUUIDsFor(tag: ResourceLocation): Set<UUID> {
        return this.tags.get(tag)
    }

    public fun getPlayersFor(tag: ResourceLocation): List<ServerPlayer> {
        return this.getUUIDsFor(tag).mapNotNull { this.minigame.server.player(it) }
    }

    internal fun serialize(): JsonArray {
        val array = JsonArray()
        for ((uuid, tags) in this.players.asMap()) {
            val json = JsonObject()
            json["uuid"] = uuid.toString()
            val tagArray = JsonArray()
            for (tag in tags) {
                tagArray.add(tag.toString())
            }
            json["tags"] = tagArray
            array.add(json)
        }
        return array
    }

    internal fun deserialize(array: JsonArray) {
        for (json in array.objects()) {
            val uuid = json.uuid("uuid")
            val tags = json.array("tags").strings()
            val parsed = tags.map(ResourceLocation::parse)
            this.players.putAll(uuid, parsed)
            for (tag in parsed) {
                this.tags.put(tag, uuid)
            }
        }
    }
}