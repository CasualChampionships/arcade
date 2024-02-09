package net.casual.arcade.minigame.managers

import net.casual.arcade.events.minigame.MinigameAddPlayerEvent
import net.casual.arcade.events.minigame.MinigameRemovePlayerEvent
import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.gui.TickableUI
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.gui.nametag.ArcadeNameTag
import net.casual.arcade.gui.sidebar.ArcadeSidebar
import net.casual.arcade.gui.tab.ArcadeTabDisplay
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.events.lobby.ReadyChecker

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
    private val bossbars: MutableList<CustomBossBar>
    private val nameTags: MutableList<ArcadeNameTag>
    private val tickables: MutableSet<TickableUI>

    private var sidebar: ArcadeSidebar?
    private var display: ArcadeTabDisplay?

    public var countdown: Countdown
    public var readier: ReadyChecker

    init {
        this.bossbars = ArrayList()
        this.nameTags = ArrayList()
        this.tickables = LinkedHashSet()

        this.countdown = TitledCountdown.DEFAULT
        this.readier = ReadyChecker.of(this.minigame)
        this.sidebar = null
        this.display = null

        this.minigame.events.register<MinigameAddPlayerEvent> { event ->
            val player = event.player
            this.bossbars.forEach { it.addPlayer(player) }
            this.nameTags.forEach { it.addPlayer(player) }
            this.sidebar?.addPlayer(player)
            this.display?.addPlayer(player)
        }
        this.minigame.events.register<MinigameRemovePlayerEvent> { event ->
            val player = event.player
            this.nameTags.forEach { it.removePlayer(player) }
            this.bossbars.forEach { it.removePlayer(player) }
            this.sidebar?.removePlayer(player)
            this.display?.removePlayer(player)
        }
    }


    /**
     * This adds a [CustomBossBar] to the minigame.
     *
     * This will be displayed to all players in the minigame.
     *
     * @param bar The bossbar to add.
     * @see CustomBossBar
     */
    public fun addBossbar(bar: CustomBossBar) {
        this.bossbars.add(bar)
        this.loadUI(bar)
    }

    /**
     * This removes a [CustomBossBar] from the minigame.
     *
     * All players who were shown the bossbar will no longer
     * be displayed the bossbar.
     *
     * @param bar The bar to remove.
     */
    public fun removeBossbar(bar: CustomBossBar) {
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
     * This adds a [ArcadeNameTag] to the minigame.
     *
     * This name tag will be applied to all players in
     * the minigame.
     *
     * @param tag The name tag to add.
     * @see ArcadeNameTag
     */
    public fun addNameTag(tag: ArcadeNameTag) {
        this.nameTags.add(tag)
        this.loadUI(tag)
    }

    /**
     * This removes a [ArcadeNameTag] from the minigame.
     *
     * All players who had the nametag will no longer be
     * displayed the nametag.
     *
     * @param tag The nametag to remove.
     */
    public fun removeNameTag(tag: ArcadeNameTag) {
        if (this.nameTags.remove(tag)) {
            this.removeUI(tag)
        }
    }

    /**
     * This removes **ALL** nametags from the minigame.
     */
    public fun removeAllNameTags() {
        this.removeAllUI(this.nameTags)
    }

    /**
     * This sets the [ArcadeSidebar] for the minigame.
     *
     * This sidebar will be displayed to all the players
     * in the minigame.
     *
     * @param sidebar The sidebar to set.
     */
    public fun setSidebar(sidebar: ArcadeSidebar) {
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
     * This sets the [ArcadeTabDisplay] for the minigame.
     *
     * This tab display will be displayed to all the players
     * in the minigame.
     *
     * @param display The tab display to set.
     */
    public fun setTabDisplay(display: ArcadeTabDisplay) {
        this.removeTabDisplay()
        this.display = display
        this.loadUI(display)
    }

    /**
     * This removes the minigame tab display.
     *
     * All players who were displayed the tab display
     * will no longer be displayed the tab display.
     */
    public fun removeTabDisplay() {
        this.removeUI(this.display)
        this.display = null
    }

    internal fun tick() {
        for (tickable in this.tickables) {
            tickable.tick()
        }
    }

    private fun loadUI(ui: PlayerUI) {
        for (player in this.minigame.getAllPlayers()) {
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