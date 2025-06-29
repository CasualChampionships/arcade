/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags

import net.casual.arcade.nametags.extensions.EntityNametagExtension
import net.casual.arcade.nametags.virtual.NametagHeight
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.ColorARGB
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

/**
 * This interface represents a custom nametag.
 *
 * You can add any implementation of this class using
 * [EntityNametagExtension.addNametag], and remove it using
 * [EntityNametagExtension.removeNametag].
 */
public interface Nametag {
    /**
     * How often the name tag should be updated.
     */
    public val updateInterval: MinecraftTimeDuration
        get() = 1.Ticks

    /**
     * This gets the height of the nametag.
     * You should change this depending on the height
     * of your nametag, by default, it is [NametagHeight.DEFAULT]
     * which corresponds to a single line of text.
     */
    public val height: NametagHeight
        get() = NametagHeight.DEFAULT

    /**
     * Gets the background color of the nametag.
     * `null` to use Minecraft's default background color.
     */
    public val backgroundColor: ColorARGB?
        get() = null

    /**
     * This gets the component that will be displayed as the name tag
     * for the given [observee].
     *
     * @param observee The entity the nametag is for.
     * @return The component to display.
     */
    public fun getComponent(observee: Entity): Component

    /**
     * This method determines whether the [observee]'s nametag
     * should be visible to the [observer].
     *
     * @param observee The entity whose nametag is being observed.
     * @param observer The player observing the nametag.
     * @return Whether the nametag should be visible.
     */
    public fun isObservable(observee: Entity, observer: ServerPlayer): Boolean

    /**
     * This method determines whether the [observee] is within range of the [observer]
     *
     * @param observee The entity whose nametag is being observed.
     * @param observer The player observing the nametag.
     * @return Whether the entity is in range.
     */
    public fun isWithinRange(observee: Entity, observer: ServerPlayer): Boolean {
        return true
    }

    /**
     * Whether the name tag should be visible through walls (when not sneaking).
     *
     * @param observee The observee whose nametag is visible or not.
     * @return Whether the nametag should be visible or not.
     */
    public fun isVisibleThroughWalls(observee: Entity): Boolean {
        return true
    }
}