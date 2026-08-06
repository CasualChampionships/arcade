/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.data

import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.virtual.visuals.VirtualVisual
import net.minecraft.server.level.ServerPlayer
import java.util.*

/**
 * A single piece of state belonging to a [VirtualVisual].
 *
 * Each value has a base value, which is what every observer
 * is shown by default, as well as optional per-player overrides.
 * If a player has no override for this value, then they are
 * shown the base value.
 *
 * Values cannot be constructed directly, instead they must be
 * registered with a [PlayerSpecificVisualData], which owns the
 * override storage and the dirty tracking:
 * ```
 * private val data = PlayerSpecificVisualData()
 *
 * val title: PlayerSpecificValue<Component> = this.data.register(CommonComponents.EMPTY)
 * ```
 *
 * @param T The type of the value.
 * @see PlayerSpecificVisualData
 */
public class PlayerSpecificValue<T: Any> internal constructor(
    private val owner: PlayerSpecificVisualData,
    internal val index: Int,
    base: T
) {
    /**
     * The bit representing this value in the dirty masks returned
     * by [PlayerSpecificVisualData.clean] and [PlayerSpecificVisualData.clean].
     */
    public val bit: Int = 1 shl this.index

    internal var base: T = base
        private set

    internal var baseDirty: Boolean = false

    /**
     * Gets the base value; the value shown to any observer
     * which does not have an override.
     *
     * @return The base value.
     */
    public fun get(): T {
        return this.base
    }

    /**
     * Gets the value shown to the given [player], which is
     * their override, if they have one, otherwise the base value.
     *
     * @param player The uuid of the player.
     * @return The value for the given [player].
     */
    public fun get(player: UUID): T {
        return this.owner.getOverride(player, this) ?: this.base
    }

    /**
     * Gets the value shown to the given [player], which is
     * their override, if they have one, otherwise the base value.
     *
     * @param player The player.
     * @return The value for the given [player].
     */
    public fun get(player: ServerPlayer): T {
        return this.get(player.uuid)
    }

    /**
     * Gets the value shown to the given [observer].
     *
     * Observers which aren't players cannot have overrides,
     * they are always shown the base value.
     *
     * @param observer The observer.
     * @return The value for the given [observer].
     */
    public fun get(observer: Observer): T {
        val player = observer.asPlayerOrNull() ?: return this.base
        return this.get(player.uuid)
    }

    /**
     * Sets the base value.
     *
     * This marks the value as dirty for every observer which
     * does not override it; observers which do override it are
     * unaffected.
     *
     * @param value The new base value.
     * @param force Whether to mark the value dirty even if unchanged.
     * @return Whether the value was marked dirty.
     */
    public fun set(value: T, force: Boolean = false): Boolean {
        if (!force && this.base == value) {
            return false
        }
        this.base = value
        this.baseDirty = true
        return true
    }

    /**
     * Overrides this value for the given [player].
     *
     * The override is kept until [setToBase] is called, even if
     * [value] happens to be equal to the current base value; a
     * player pinned to a value will not follow later changes to
     * the base value.
     *
     * @param player The uuid of the player to override for.
     * @param value The value to show the given [player].
     * @param force Whether to mark the value dirty even if unchanged.
     * @return Whether the value the player is shown changed.
     */
    public fun set(player: UUID, value: T, force: Boolean = false): Boolean {
        val previous = this.get(player)
        this.owner.setOverride(player, this, value)
        if (force || previous != value) {
            this.owner.markDirty(player, this.bit)
            return true
        }
        return false
    }

    /**
     * Overrides this value for the given [player].
     *
     * @param player The player to override for.
     * @param value The value to show the given [player].
     * @param force Whether to mark the value dirty even if unchanged.
     * @return Whether the value the player is shown changed.
     * @see set
     */
    public fun set(player: ServerPlayer, value: T, force: Boolean = false): Boolean {
        return this.set(player.uuid, value, force)
    }

    /**
     * Removes the given [player]'s override, if they have one,
     * so that they are shown the base value again, and will
     * follow any future changes to it.
     *
     * @param player The uuid of the player to remove the override for.
     * @param force Whether to mark the value dirty even if unchanged.
     * @return Whether the player had an override which was removed.
     */
    public fun setToBase(player: UUID, force: Boolean = false): Boolean {
        val previous = this.owner.removeOverride(player, this) ?: return false
        if (force || previous != this.base) {
            this.owner.markDirty(player, this.bit)
        }
        return true
    }

    /**
     * Removes the given [player]'s override, if they have one.
     *
     * @param player The player to remove the override for.
     * @param force Whether to mark the value dirty even if unchanged.
     * @return Whether the player had an override which was removed.
     * @see setToBase
     */
    public fun setToBase(player: ServerPlayer, force: Boolean = false): Boolean {
        return this.setToBase(player.uuid, force)
    }

    /**
     * Checks whether the given [player] overrides this value.
     *
     * @param player The uuid of the player.
     * @return Whether the player has an override.
     */
    public fun isOverridden(player: UUID): Boolean {
        return this.owner.isOverridden(player, this)
    }

    /**
     * Checks whether the given [player] overrides this value.
     *
     * @param player The player.
     * @return Whether the player has an override.
     */
    public fun isOverridden(player: ServerPlayer): Boolean {
        return this.isOverridden(player.uuid)
    }
}
