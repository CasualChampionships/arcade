package net.casual.arcade.minigame.managers

import net.casual.arcade.events.player.PlayerJoinEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.ListenerFlags
import net.casual.arcade.minigame.events.MinigameAddPlayerEvent
import net.casual.arcade.minigame.events.MinigameRemovePlayerEvent
import net.casual.arcade.minigame.lobby.ReadyChecker
import net.casual.arcade.visuals.bossbar.CustomBossbar
import net.casual.arcade.visuals.core.PlayerUI
import net.casual.arcade.visuals.core.TickableUI
import net.casual.arcade.visuals.countdown.Countdown
import net.casual.arcade.visuals.countdown.TitledCountdown
import net.casual.arcade.visuals.nametag.PlayerNameTag
import net.casual.arcade.visuals.sidebar.Sidebar
import net.casual.arcade.visuals.tab.PlayerListDisplay
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

/**
 * This manager handles all the UI elements that can be added
 * to a minigame.
 *
 * It will handle displaying and updating UI to all the
 * players in a given minigame.
 *
 * @see Minigame.ui
 */
public class MinigameUIManager(
    private val minigame: Minigame<*>
) {
    private val bossbars: MutableList<CustomBossbar>
    private val nametags: MutableList<PlayerNameTag>
    private val tickables: MutableSet<TickableUI>

    private var sidebar: Sidebar?
    private var display: PlayerListDisplay?

    public var countdown: Countdown
    public var readier: ReadyChecker

    init {
        this.bossbars = ArrayList()
        this.nametags = ArrayList()
        this.tickables = LinkedHashSet()

        this.countdown = TitledCountdown.titled()
        this.readier = ReadyChecker.of(this.minigame)
        this.sidebar = null
        this.display = null

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
        this.minigame.events.register<PlayerJoinEvent>(priority = 1_000, flags = ListenerFlags.NONE) {
            this.display?.onPlayerJoin(it.player)
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
        this.loadUI(bar)
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
            this.removeUI(bar)
        }
    }

    /**
     * This removes **ALL** bossbars from the minigame.
     */
    public fun removeAllBossbars() {
        this.removeAllUI(this.bossbars)
    }

    /**
     * This adds a [PlayerNameTag] to the minigame.
     *
     * This name tag will be applied to all players in
     * the minigame.
     *
     * @param tag The name tag to add.
     * @see PlayerNameTag
     */
    public fun addNameTag(tag: PlayerNameTag) {
        this.nametags.add(tag)
        this.loadUI(tag)
    }

    /**
     * This removes a [PlayerNameTag] from the minigame.
     *
     * All players who had the nametag will no longer be
     * displayed the nametag.
     *
     * @param tag The nametag to remove.
     */
    public fun removeNameTag(tag: PlayerNameTag) {
        if (this.nametags.remove(tag)) {
            this.removeUI(tag)
        }
    }

    /**
     * This removes **ALL** nametags from the minigame.
     */
    public fun removeAllNameTags() {
        this.removeAllUI(this.nametags)
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
        this.loadUI(sidebar)
    }

    /**
     * This removes the minigame sidebar.
     *
     * All players who were displayed the sidebar
     * will no longer be displayed the sidebar.
     */
    public fun removeSidebar() {
        this.removeUI(this.sidebar)
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
        this.loadUI(display)
    }

    /**
     * This removes the minigame tab display.
     *
     * All players who were displayed the tab display
     * will no longer be displayed the tab display.
     */
    public fun removePlayerListDisplay() {
        this.removeUI(this.display)
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

    private fun loadUI(ui: PlayerUI) {
        for (player in this.minigame.players) {
            ui.addPlayer(player)
        }
        if (ui is TickableUI) {
            this.tickables.add(ui)
        }
    }

    private fun removeUI(ui: PlayerUI?) {
        if (ui != null) {
            ui.clearPlayers()
            if (ui is TickableUI) {
                this.tickables.remove(ui)
            }
        }
    }

    private fun removeAllUI(uis: MutableCollection<out PlayerUI>) {
        for (ui in uis) {
            this.removeUI(ui)
        }
        uis.clear()
    }
}