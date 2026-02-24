/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.google.common.collect.HashMultimap
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameCloseEvent
import net.casual.arcade.minigame.events.MinigameCompleteEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.utils.AdvancementUtils.copyWithoutToast
import net.casual.arcade.utils.player.grantAdvancementSilently
import net.casual.arcade.utils.player.revokeAdvancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementNode
import net.minecraft.advancements.AdvancementTree
import net.minecraft.advancements.TreeNodePosition
import net.minecraft.core.UUIDUtil
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import java.util.*
import kotlin.jvm.optionals.getOrNull

/**
 * This class manages the advancements of a minigame.
 *
 * All advancements added to this manager are local to the
 * minigame only and do not exist outside the context
 * of the minigame.
 *
 * @see Minigame.advancements
 */
public class MinigameAdvancementManager(
    private val minigame: Minigame
) {
    private val tree = AdvancementTree()
    private val reloaded = HashMultimap.create<UUID, Identifier>()

    private val players = HashMultimap.create<UUID, Identifier>()

    init {
        this.minigame.events.register<MinigameAddPlayerEvent> { event ->
            this.reloadFor(event.player)
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> { event ->
            this.unloadFor(event.player)
        }
        this.minigame.events.register<MinigameCompleteEvent> {
            this.syncForAll()
        }
        this.minigame.events.register<MinigameCloseEvent> {
            if (!this.minigame.completed) {
                this.syncForAll()
            }
        }
        this.minigame.events.register<PlayerLeaveEvent> { (player) ->
            this.syncFor(player)
            this.reloaded.removeAll(player.uuid)
        }
        this.minigame.events.register<PlayerClientboundPacketEvent>(this::onPlayerClientboundPacket)
    }

    /**
     * This adds an advancement to the minigame.
     *
     * @param advancement The advancement to add.
     */
    public fun add(advancement: AdvancementHolder) {
        this.tree.addAll(listOf(advancement))
        val node = this.tree.get(advancement) ?: return
        TreeNodePosition.run(node.root())
    }

    /**
     * This adds a collection of advancements to the minigame.
     *
     * @param advancements The advancements to add.
     */
    public fun addAll(advancements: Collection<AdvancementHolder>) {
        this.tree.addAll(advancements)

        for (node in this.tree.roots()) {
            if (node.holder().value().display().isPresent) {
                TreeNodePosition.run(node)
            }
        }
    }

    /**
     * This gets an advancement by its [Identifier].
     *
     * @param id The [Identifier] of the advancement.
     * @return The advancement or null if it does not exist.
     */
    public fun get(id: Identifier): AdvancementHolder? {
        return this.getNode(id)?.holder()
    }

    public fun getNode(id: Identifier): AdvancementNode? {
        return this.tree.get(id)
    }

    public fun all(): Collection<AdvancementHolder> {
        return this.tree.nodes().map { it.holder() }
    }

    public fun getFor(uuid: UUID): Collection<Identifier> {
        return this.players.get(uuid)
    }

    public fun reloadFor(player: ServerPlayer) {
        val ids = this.players.get(player.uuid)
        if (ids.isNotEmpty()) {
            this.reloaded.putAll(player.uuid, ids)
            for (id in ids) {
                val holder = this.get(id) ?: continue
                player.grantAdvancementSilently(holder)
            }
        }
    }

    private fun unloadFor(player: ServerPlayer) {
        this.syncFor(player)
        this.reloaded.removeAll(player.uuid)
        for (advancement in this.tree.nodes()) {
            player.revokeAdvancement(advancement.holder())
        }
    }

    private fun syncFor(player: ServerPlayer) {
        val synced = this.players.get(player.uuid)
        synced.clear()
        for (advancement in this.tree.nodes()) {
            val holder = advancement.holder()
            if (player.advancements.getOrStartProgress(holder).isDone) {
                synced.add(holder.id())
            }
        }
    }

    private fun syncForAll() {
        for (player in this.minigame.players) {
            this.syncFor(player)
        }
    }

    private fun onPlayerClientboundPacket(event: PlayerClientboundPacketEvent) {
        val (player, packet) = event
        if (packet !is ClientboundUpdateAdvancementsPacket) {
            return
        }

        val reloaded = this.reloaded.removeAll(player.uuid)
        if (packet.shouldReset() || reloaded.isEmpty()) {
            return
        }

        val copy = ArrayList<AdvancementHolder>()
        for (added in packet.added) {
            if (!reloaded.contains(added.id)) {
                copy.add(added)
                continue
            }
            copy.add(added.copyWithoutToast())
        }
        event.packet = ClientboundUpdateAdvancementsPacket(
            false, copy, packet.removed, packet.progress, packet.shouldShowAdvancements()
        )
    }

    internal fun serialize(list: ValueOutput.ValueOutputList) {
        for ((player, advancements) in this.players.asMap()) {
            val output = list.addChild()
            output.store("uuid", UUIDUtil.STRING_CODEC, player)
            output.store("advancements", Identifier.CODEC.listOf(), advancements.toList())
        }
    }

    internal fun deserialize(list: ValueInput.ValueInputList) {
        for (input in list) {
            val uuid = input.read("uuid", UUIDUtil.STRING_CODEC).getOrNull() ?: continue
            val advancements = input.read("advancements", Identifier.CODEC.listOf()).getOrNull() ?: continue
            this.players.putAll(uuid, advancements)
        }
    }
}