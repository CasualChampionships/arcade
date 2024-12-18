/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.utils.AdvancementUtils.copyWithoutToast
import net.casual.arcade.utils.PlayerUtils.grantAdvancementSilently
import net.casual.arcade.utils.PlayerUtils.revokeAdvancement
import net.minecraft.advancements.AdvancementHolder
import net.minecraft.advancements.AdvancementNode
import net.minecraft.advancements.AdvancementTree
import net.minecraft.advancements.TreeNodePosition
import net.minecraft.network.protocol.game.ClientboundUpdateAdvancementsPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import java.util.*

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
    private val reloaded = Object2ObjectOpenHashMap<UUID, Set<ResourceLocation>>()

    init {
        this.minigame.events.register<MinigameAddPlayerEvent> { event ->
            this.reloadFor(event.player)
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> { event ->
            this.unloadFor(event.player)
        }
        this.minigame.events.register<PlayerLeaveEvent> { (player) ->
            this.reloaded.remove(player.uuid)
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
     * This gets an advancement by its [ResourceLocation].
     *
     * @param id The [ResourceLocation] of the advancement.
     * @return The advancement or null if it does not exist.
     */
    public fun get(id: ResourceLocation): AdvancementHolder? {
        return this.getNode(id)?.holder()
    }

    public fun getNode(id: ResourceLocation): AdvancementNode? {
        return this.tree.get(id)
    }

    public fun all(): Collection<AdvancementHolder> {
        return this.tree.nodes().map { it.holder() }
    }

    public fun reloadFor(player: ServerPlayer) {
        val holders = this.minigame.data.getAdvancements(player.uuid)
        if (holders.isNotEmpty()) {
            this.reloaded[player.uuid] = holders.mapTo(HashSet()) { it.id }
            for (holder in holders) {
                player.grantAdvancementSilently(holder)
            }
        }
    }

    private fun unloadFor(player: ServerPlayer) {
        this.reloaded.remove(player.uuid)
        for (advancement in this.tree.nodes()) {
            player.revokeAdvancement(advancement.holder())
        }
    }

    private fun onPlayerClientboundPacket(event: PlayerClientboundPacketEvent) {
        val (player, packet) = event
        if (packet !is ClientboundUpdateAdvancementsPacket) {
            return
        }

        val reloaded = this.reloaded.remove(player.uuid) ?: return
        if (packet.shouldReset()) {
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
        event.packet = ClientboundUpdateAdvancementsPacket(false, copy, packet.removed, packet.progress)
    }
}