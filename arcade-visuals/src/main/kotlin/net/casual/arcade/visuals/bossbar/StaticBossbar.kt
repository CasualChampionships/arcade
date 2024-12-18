/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.bossbar

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

/**
 * This class is an implementation of [CustomBossbar] that
 * doesn't update and remains static.
 *
 * @param title The title of the bar.
 * @param progress The progress of the bar.
 * @param colour The colour of the bar.
 * @param overlay The overlay of the bar.
 * @param dark Whether the player's world should be dark.
 * @param music Whether the player should player boss music.
 * @param fog Whether the player's world should have fog.
 * @see CustomBossbar
 */
public data class StaticBossbar(
    /**
     * The title of the bar.
     */
    val title: Component,
    /**
     * The progress of the bar.
     */
    val progress: Float = 1.0F,
    /**
     * The colour of the bar.
     */
    val colour: BossBarColor = BossBarColor.WHITE,
    /**
     * The overlay of the bar.
     */
    val overlay: BossBarOverlay = BossBarOverlay.PROGRESS,
    /**
     * Whether the player's world should be dark.
     */
    val dark: Boolean = false,
    /**
     * Whether the player should player boss music.
     */
    val music: Boolean = false,
    /**
     * Whether the player's world should have fog.
     */
    val fog: Boolean = false
): CustomBossbar() {
    init {
        // We're not updating anything anyways...
        this.setInterval(Int.MAX_VALUE)
    }

    /**
     * This gets the title of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the component.
     * @return The [Component] to display as the title of the [CustomBossbar].
     */
    override fun getTitle(player: ServerPlayer): Component {
        return this.title
    }

    /**
     * This gets the progress of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    override fun getProgress(player: ServerPlayer): Float {
        return this.progress
    }

    /**
     * This gets the colour of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the colour.
     * @return The [BossBarColor] to set the bossbar to.
     */
    override fun getColour(player: ServerPlayer): BossBarColor {
        return this.colour
    }

    /**
     * This gets the overlay of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the overlay.
     * @return The [BossBarOverlay] to set the bossbar to.
     */
    override fun getOverlay(player: ServerPlayer): BossBarOverlay {
        return this.overlay
    }

    /**
     * This sets whether the player's world is dark or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should be dark.
     */
    override fun isDark(player: ServerPlayer): Boolean {
        return this.dark
    }

    /**
     * This sets whether the player's should be played boss
     * music in the end dimension.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player should play boss music.
     */
    override fun hasMusic(player: ServerPlayer): Boolean {
        return this.music
    }

    /**
     * This sets whether the player's world has fog or not.
     *
     * @param player The player being displayed the bar.
     * @return Whether the player's world should have fog.
     */
    override fun hasFog(player: ServerPlayer): Boolean {
        return this.fog
    }
}