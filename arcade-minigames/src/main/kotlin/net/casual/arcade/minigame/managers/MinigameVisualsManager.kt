/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import it.unimi.dsi.fastutil.objects.ReferenceArrayList
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.visuals.bossbar.CustomBossbar
import net.casual.arcade.visuals.core.TickableVisualElement
import net.casual.arcade.visuals.core.VisualElement
import net.casual.arcade.visuals.nametag.PlayerNametag
import net.casual.arcade.visuals.ready.ReadyBroadcaster
import net.casual.arcade.visuals.ready.chat.PlayerChatReadyBroadcaster
import net.casual.arcade.visuals.ready.chat.TeamChatReadyBroadcaster
import net.casual.arcade.visuals.sidebar.Sidebar
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.casual.arcade.visuals.transition.TitledCountdown
import net.casual.arcade.visuals.transition.Transition
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundPlayerInfoUpdatePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import java.util.function.Consumer

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
    private val bossbars = ReferenceArrayList<CustomBossbar>()
    private val nametags = ReferenceArrayList<PlayerNametag>()
    private val tickables = ReferenceLinkedOpenHashSet<TickableVisualElement>()

    private var sidebar: Sidebar? = null
    private var display: PlayerListDisplay? = null

    public var countdown: Transition = TitledCountdown.titled()

    public var playerReadyBroadcaster: ReadyBroadcaster<ServerPlayer> = PlayerChatReadyBroadcaster { this.minigame.chat.broadcast(it) }
    public var teamReadyBroadcaster: ReadyBroadcaster<PlayerTeam> = TeamChatReadyBroadcaster { this.minigame.chat.broadcast(it) }

    init {
        this.minigame.events.register<MinigameAddPlayerEvent> { event ->
            val player = event.player
            this.bossbars.forEach { it.addPlayer(player) }
            this.nametags.forEach { it.addPlayer(player) }
            this.sidebar?.addPlayer(player)
            this.display?.addPlayer(player)
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> { event ->
            val player = event.player
            this.nametags.forEach { it.removePlayer(player) }
            this.bossbars.forEach { it.removePlayer(player) }
            this.sidebar?.removePlayer(player)
            this.display?.removePlayer(player)
        }
        this.minigame.events.register<PlayerClientboundPacketEvent> { event ->
            val packet = event.packet
            if (packet is ClientboundPlayerInfoUpdatePacket) {
                event.packet = this.display?.replacePlayerInfoUpdatePacket(event.player, packet) ?: event.packet
            }
        }
    }


    /**
     * This adds a [CustomBossbar] to the minigame.
     *
     * This will be displayed to all players in the minigame.
     *
     * @param bar The bossbar to add.
     * @see CustomBossbar
     */
    public fun addBossbar(bar: CustomBossbar) {
        this.bossbars.add(bar)
        this.loadVisual(bar)
    }

    /**
     * This removes a [CustomBossbar] from the minigame.
     *
     * All players who were shown the bossbar will no longer
     * be displayed the bossbar.
     *
     * @param bar The bar to remove.
     */
    public fun removeBossbar(bar: CustomBossbar) {
        if (this.bossbars.remove(bar)) {
            this.removeVisual(bar)
        }
    }

    /**
     * This removes **ALL** bossbars from the minigame.
     */
    public fun removeAllBossbars() {
        this.removeAllVisuals(this.bossbars)
    }

    /**
     * This adds a [PlayerNametag] to the minigame.
     *
     * This name tag will be applied to all players in
     * the minigame.
     *
     * @param tag The name tag to add.
     * @see PlayerNametag
     */
    public fun addNametag(tag: PlayerNametag) {
        this.nametags.add(tag)
        this.loadVisual(tag)
    }

    /**
     * This removes a [PlayerNametag] from the minigame.
     *
     * All players who had the nametag will no longer be
     * displayed the nametag.
     *
     * @param tag The nametag to remove.
     */
    public fun removeNametag(tag: PlayerNametag) {
        if (this.nametags.remove(tag)) {
            this.removeVisual(tag)
        }
    }

    /**
     * This removes **ALL** nametags from the minigame.
     */
    public fun removeAllNametags() {
        this.removeAllVisuals(this.nametags)
    }

    /**
     * This sets the [Sidebar] for the minigame.
     *
     * This sidebar will be displayed to all the players
     * in the minigame.
     *
     * @param sidebar The sidebar to set.
     */
    public fun setSidebar(sidebar: Sidebar) {
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
     * This sets the [PlayerListDisplay] for the minigame.
     *
     * This tab display will be displayed to all the players
     * in the minigame.
     *
     * @param display The tab display to set.
     */
    public fun setPlayerListDisplay(display: PlayerListDisplay) {
        this.removePlayerListDisplay()
        this.display = display
        this.loadVisual(display)
    }

    /**
     * This removes the minigame tab display.
     *
     * All players who were displayed the tab display
     * will no longer be displayed the tab display.
     */
    public fun removePlayerListDisplay() {
        this.removeVisual(this.display)
        this.display = null
    }

    internal fun tick(server: MinecraftServer) {
        for (tickable in this.tickables.toList()) {
            if (!this.minigame.paused || tickable.shouldTickWhenPaused()) {
                tickable.tick(server)
            }
        }
    }

    internal fun resendUI(player: ServerPlayer, sender: Consumer<Packet<ClientGamePacketListener>>) {
        this.bossbars.forEach { it.resendToPlayer(player, sender) }
        this.sidebar?.resendToPlayer(player, sender)
        this.display?.resendToPlayer(player, sender)
    }

    private fun loadVisual(element: VisualElement) {
        for (player in this.minigame.players) {
            element.addPlayer(player)
        }
        if (element is TickableVisualElement) {
            this.tickables.add(element)
        }
    }

    private fun removeVisual(element: VisualElement?) {
        if (element != null) {
            element.clearPlayers()
            if (element is TickableVisualElement) {
                this.tickables.remove(element)
            }
        }
    }

    private fun removeAllVisuals(visuals: MutableCollection<out VisualElement>) {
        for (ui in visuals) {
            this.removeVisual(ui)
        }
        visuals.clear()
    }
}