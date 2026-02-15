/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.google.common.collect.HashMultimap
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddTagEvent
import net.casual.arcade.minigame.events.MinigameRemoveTagEvent
import net.casual.arcade.utils.PlayerUtils.player
import net.minecraft.core.UUIDUtil
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*
import kotlin.jvm.optionals.getOrNull

public class MinigameTagManager(
    private val minigame: Minigame
) {
    private val players = HashMultimap.create<UUID, Identifier>()
    private val tags = HashMultimap.create<Identifier, UUID>()

    public fun get(uuid: UUID): Set<Identifier> {
        return this.players.get(uuid)
    }

    public fun get(player: ServerPlayer): Set<Identifier> {
        return this.get(player.uuid)
    }

    public fun has(uuid: UUID, tag: Identifier): Boolean {
        return this.players.containsEntry(uuid, tag)
    }

    public fun has(player: ServerPlayer, tag: Identifier): Boolean {
        return this.has(player.uuid, tag)
    }

    public fun add(uuid: UUID, tag: Identifier): Boolean {
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

    public fun add(player: ServerPlayer, tag: Identifier): Boolean {
        return this.add(player.uuid, tag)
    }

    public fun remove(uuid: UUID, tag: Identifier): Boolean {
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

    public fun remove(player: ServerPlayer, tag: Identifier): Boolean {
        return this.remove(player.uuid, tag)
    }

    public fun clear(tag: Identifier) {
        val players = this.tags.removeAll(tag)
        for (player in players) {
            this.remove(player, tag)
        }
    }

    public fun getUUIDsFor(tag: Identifier): Set<UUID> {
        return this.tags.get(tag)
    }

    public fun getPlayersFor(tag: Identifier): List<ServerPlayer> {
        return this.getUUIDsFor(tag).mapNotNull { this.minigame.server.player(it) }
    }

    internal fun serialize(list: ValueOutput.ValueOutputList) {
        for (entry in this.players.asMap()) {
            val output = list.addChild()
            output.store("uuid", UUIDUtil.STRING_CODEC, entry.key)
            val tags = output.list("tags", Identifier.CODEC)
            for (tag in entry.value) {
                tags.add(tag)
            }
        }
    }

    internal fun deserialize(list: ValueInput.ValueInputList) {
        for (input in list) {
            val uuid = input.read("uuid", UUIDUtil.STRING_CODEC).getOrNull() ?: continue
            val tags = input.listOrEmpty("tags", Identifier.CODEC)
            this.players.putAll(uuid, tags)
            for (tag in tags) {
                this.tags.put(tag, uuid)
            }
        }
    }
}