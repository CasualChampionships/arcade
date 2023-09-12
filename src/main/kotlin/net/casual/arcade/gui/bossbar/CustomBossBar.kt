package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.utils.BossbarUtils.bossbars
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Mth
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay
import java.util.*

/**
 * A custom boss bar implementation that can be
 * displayed to players individually by providing
 * the boss bar on a per-player basis.
 *
 * Each of the components of the boss bar is
 * updated as per the [interval] set. By default,
 * this is set to `1` which updates all the components
 * **every** tick.
 *
 * You can inherit this class to implement the
 * abstract methods, or you can use [ArcadeBossbar]
 * and provide suppliers for the components
 * of the boss bar.
 *
 * Once you have a [CustomBossBar] implementation
 * you can simply call [addPlayer] to display
 * the boss bar to them. You may also [removePlayer]
 * or [clearPlayers], for more information see [PlayerUI].
 *
 * @see ArcadeBossbar
 * @see PlayerUI
 */
public abstract class CustomBossBar: PlayerUI() {
    internal val uuid: UUID = Mth.createInsecureUUID()

    /**
     * This gets the title of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the component.
     * @return The [Component] to display as the title of the [CustomBossBar].
     */
    public abstract fun getTitle(player: ServerPlayer): Component

    /**
     * This gets the progress of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    public abstract fun getProgress(player: ServerPlayer): Float

    /**
     * This gets the colour of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the colour.
     * @return The [BossBarColor] to set the bossbar to.
     */
    public abstract fun getColour(player: ServerPlayer): BossBarColor

    /**
     * This gets the overlay of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the overlay.
     * @return The [BossBarOverlay] to set the bossbar to.
     */
    public abstract fun getOverlay(player: ServerPlayer): BossBarOverlay

    /**
     * This sets whether the player's world is dark or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should be dark.
     */
    public open fun isDark(player: ServerPlayer): Boolean {
        return false
    }

    /**
     * This sets whether the player's should be played boss
     * music in the end dimension.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player should play boss music.
     */
    public open fun hasMusic(player: ServerPlayer): Boolean {
        return false
    }

    /**
     * This sets whether the player's world has fog or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should have fog.
     */
    public open fun hasFog(player: ServerPlayer): Boolean {
        return false
    }

    final override fun onAddPlayer(player: ServerPlayer) {
        player.bossbars.add(this)
    }

    final override fun onRemovePlayer(player: ServerPlayer) {
        player.bossbars.remove(this)
    }
}