/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.nametags.Nametag
import net.casual.arcade.nametags.extensions.EntityNametagExtension.Companion.nametagExtension
import net.casual.arcade.observer.utils.asObserver
import net.casual.arcade.virtual.visuals.VirtualVisual
import net.casual.arcade.virtual.visuals.bossbar.VirtualBossbar
import net.casual.arcade.virtual.visuals.ready.ReadyBroadcaster
import net.casual.arcade.virtual.visuals.ready.chat.PlayerChatReadyBroadcaster
import net.casual.arcade.virtual.visuals.ready.chat.TeamChatReadyBroadcaster
import net.casual.arcade.virtual.visuals.sidebar.VirtualSidebar
import net.casual.arcade.virtual.visuals.tab.VirtualPlayerList
import net.casual.arcade.virtual.visuals.transition.TitledCountdown
import net.casual.arcade.virtual.visuals.transition.Transition
import net.casual.arcade.virtual.visuals.utils.startObservingAndSendPackets
import net.casual.arcade.virtual.visuals.utils.stopObservingAndSendPackets
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam

/**
 * This manager handles all the UI elements that can be added
 * to a minigame.
 *
 * It will handle displaying and updating UI to all the
 * players in a given minigame.
 *
 * @see Minigame.visuals
 */
public class MinigameVisualsManager(
    private val minigame: Minigame
) {
    private val bossbars = ReferenceArrayList<VirtualBossbar>()
    private val nametags = ReferenceArrayList<Nametag>()
    private val visuals = ReferenceLinkedOpenHashSet<VirtualVisual>()

    private var sidebar: VirtualSidebar? = null
    private var display: VirtualPlayerList? = null

    public var countdown: Transition = TitledCountdown.titled()

    public var playerReadyBroadcaster: ReadyBroadcaster<ServerPlayer> = PlayerChatReadyBroadcaster { this.minigame.chat.broadcast(it) }
    public var teamReadyBroadcaster: ReadyBroadcaster<PlayerTeam> = TeamChatReadyBroadcaster { this.minigame.chat.broadcast(it) }

    init {
        this.minigame.events.register<MinigameAddPlayerEvent> { event ->
            val player = event.player
            val observer = player.asObserver()
            for (visual in this.visuals) {
                visual.startObservingAndSendPackets(observer)
            }
            for (nametag in this.nametags) {
                player.nametagExtension.add(nametag)
            }
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> { event ->
            val player = event.player
            val observer = player.asObserver()
            for (visual in this.visuals.toList()) {
                visual.stopObservingAndSendPackets(observer)
            }
            for (nametag in this.nametags) {
                player.nametagExtension.remove(nametag)
            }
        }
        this.minigame.events.register<PlayerClientboundPacketEvent> { event ->
            val packet = event.packet
            if (packet is ClientboundPlayerInfoUpdatePacket) {
                event.packet = this.display?.replacePlayerInfoUpdatePacket(event.player, packet) ?: event.packet
            }
        }
    }

    /**
     * This adds a [VirtualBossbar] to the minigame.
     *
     * This will be displayed to all players in the minigame.
     *
     * @param bar The bossbar to add.
     * @see VirtualBossbar
     */
    public fun addBossbar(bar: VirtualBossbar) {
        this.bossbars.add(bar)
        this.loadVisual(bar)
    }

    /**
     * This removes a [VirtualBossbar] from the minigame.
     *
     * All players who were shown the bossbar will no longer
     * be displayed the bossbar.
     *
     * @param bar The bar to remove.
     */
    public fun removeBossbar(bar: VirtualBossbar) {
        if (this.bossbars.remove(bar)) {
            this.removeVisual(bar)
        }
    }

    /**
     * This removes **ALL** bossbars from the minigame.
     */
    public fun removeAllBossbars() {
        for (bar in this.bossbars) {
            this.removeVisual(bar)
        }
        this.bossbars.clear()
    }

    /**
     * This adds a [Nametag] to the minigame.
     *
     * This name tag will be applied to all players in
     * the minigame.
     *
     * @param nametag The name tag to add.
     * @see Nametag
     */
    public fun addNametag(nametag: Nametag) {
        this.nametags.add(nametag)
        for (player in this.minigame.players) {
            player.nametagExtension.add(nametag)
        }
    }

    /**
     * This removes a [Nametag] from the minigame.
     *
     * All players who had the nametag will no longer be
     * displayed the nametag.
     *
     * @param nametag The nametag to remove.
     */
    public fun removeNametag(nametag: Nametag) {
        if (this.nametags.remove(nametag)) {
            for (player in this.minigame.players) {
                player.nametagExtension.remove(nametag)
            }
        }
    }

    /**
     * This removes **ALL** nametags from the minigame.
     */
    public fun removeAllNametags() {
        for (nametag in this.nametags) {
            for (player in this.minigame.players) {
                player.nametagExtension.remove(nametag)
            }
        }
        this.nametags.clear()
    }

    /**
     * This sets the [VirtualSidebar] for the minigame.
     *
     * This sidebar will be displayed to all the players
     * in the minigame.
     *
     * @param sidebar The sidebar to set.
     */
    public fun setSidebar(sidebar: VirtualSidebar) {
        this.removeSidebar()
        this.sidebar = sidebar
        this.loadVisual(sidebar)
    }

    /**
     * This removes the minigame sidebar.
     *
     * All players who were displayed the sidebar
     * will no longer be displayed the sidebar.
     */
    public fun removeSidebar() {
        this.removeVisual(this.sidebar)
        this.sidebar = null
    }

    /**
     * This sets the [VirtualPlayerList] for the minigame.
     *
     * This player list will be displayed to all the players
     * in the minigame.
     *
     * @param display The player list to set.
     */
    public fun setPlayerListDisplay(display: VirtualPlayerList) {
        this.removePlayerListDisplay()
        this.display = display
        this.loadVisual(display)
    }

    /**
     * This removes the minigame player list.
     *
     * All players who were displayed the player list
     * will no longer be displayed the player list.
     */
    public fun removePlayerListDisplay() {
        this.removeVisual(this.display)
        this.display = null
    }

    internal fun tick() {
        for (visual in this.visuals.toList()) {
            if (!this.minigame.paused || visual.shouldTickWhenPaused()) {
                visual.tick()
            }
        }
    }

    private fun loadVisual(visual: VirtualVisual) {
        this.visuals.add(visual)
        for (player in this.minigame.players) {
            visual.startObservingAndSendPackets(player.asObserver())
        }
    }

    private fun removeVisual(visual: VirtualVisual?) {
        if (visual != null && this.visuals.remove(visual)) {
            for (observer in visual.observers.toList()) {
                visual.stopObservingAndSendPackets(observer)
            }
        }
    }
}
