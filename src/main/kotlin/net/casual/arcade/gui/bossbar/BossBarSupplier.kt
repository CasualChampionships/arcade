package net.casual.arcade.gui.bossbar

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

public interface BossBarSupplier {
    /**
     * This gets the title of the [BossBarSupplier] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the component.
     * @return The [Component] to display as the title of the [BossBarSupplier].
     */
    public fun getTitle(player: ServerPlayer): Component

    /**
     * This gets the progress of the [BossBarSupplier] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    public fun getProgress(player: ServerPlayer): Float {
        return 1.0F
    }

    /**
     * This gets the colour of the [BossBarSupplier] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the colour.
     * @return The [BossBarColor] to set the bossbar to.
     */
    public fun getColour(player: ServerPlayer): BossBarColor {
        return BossBarColor.WHITE
    }

    /**
     * This gets the overlay of the [BossBarSupplier] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the overlay.
     * @return The [BossBarOverlay] to set the bossbar to.
     */
    public fun getOverlay(player: ServerPlayer): BossBarOverlay {
        return BossBarOverlay.PROGRESS
    }

    /**
     * This sets whether the player's world is dark or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should be dark.
     */
    public fun isDark(player: ServerPlayer): Boolean {
        return false
    }

    /**
     * This sets whether the player's should be played boss
     * music in the end dimension.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player should play boss music.
     */
    public fun hasMusic(player: ServerPlayer): Boolean {
        return false
    }

    /**
     * This sets whether the player's world has fog or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should have fog.
     */
    public fun hasFog(player: ServerPlayer): Boolean {
        return false
    }
}