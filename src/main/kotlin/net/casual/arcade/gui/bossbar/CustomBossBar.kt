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
abstract class CustomBossBar: PlayerUI() {
    internal val uuid: UUID = Mth.createInsecureUUID()

    /**
     * This gets the title of the [CustomBossBar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the component.
     * @return The [Component] to display as the title of the [CustomBossBar].
     */
    abstract fun getTitle(player: ServerPlayer): Component

    abstract fun getProgress(player: ServerPlayer): Float

    abstract fun getColour(player: ServerPlayer): BossBarColor

    abstract fun getOverlay(player: ServerPlayer): BossBarOverlay

    open fun isDark(player: ServerPlayer): Boolean {
        return false
    }

    open fun hasMusic(player: ServerPlayer): Boolean {
        return false
    }

    open fun hasFog(player: ServerPlayer): Boolean {
        return false
    }

    override fun onAddPlayer(player: ServerPlayer) {
        player.bossbars.add(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.bossbars.remove(this)
    }

    companion object {
        fun constant(
            title: Component,
            progress: Float = 1.0F,
            colour: BossBarColor = BossBarColor.WHITE,
            overlay: BossBarOverlay = BossBarOverlay.PROGRESS,
            dark: Boolean = false,
            music: Boolean = false,
            fog: Boolean = false
        ): CustomBossBar {
            return object: CustomBossBar() {
                override fun getTitle(player: ServerPlayer) = title

                override fun getProgress(player: ServerPlayer) = progress

                override fun getColour(player: ServerPlayer) = colour

                override fun getOverlay(player: ServerPlayer) = overlay

                override fun isDark(player: ServerPlayer) = dark

                override fun hasMusic(player: ServerPlayer) = music

                override fun hasFog(player: ServerPlayer) = fog
            }
        }
    }
}