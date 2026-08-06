/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.visuals.data

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.virtual.visuals.VirtualVisual
import java.util.*

/**
 * This class holds all the [PlayerSpecificValue]s belonging to
 * a [VirtualVisual], as well as every player-specific override
 * of those values, and tracks which of them have changed since
 * they were last sent.
 *
 * Each registered value is assigned a bit, see [PlayerSpecificValue.bit],
 * and changes are reported as an [Int] mask of those bits. This lets a
 * visual map any group of changed values onto the single packet that
 * carries them, without allocating per observer:
 * ```
 * private val data = PlayerSpecificVisualData()
 *
 * val color = this.data.register(BossBarColor.WHITE)
 * val overlay = this.data.register(BossBarOverlay.PROGRESS)
 *
 * private val style = this.color.bit or this.overlay.bit
 *
 * override fun tick() {
 *     val base = this.data.clean()
 *     this.observers.broadcast { observer ->
 *         val player = observer.asPlayerOrNull()
 *         val dirty = if (player != null) this.data.clean(player.uuid, base) else base
 *         if ((dirty and this.style) != 0) {
 *             observer.send(...)
 *         }
 *     }
 * }
 * ```
 *
 * @see PlayerSpecificValue
 */
public class PlayerSpecificVisualData {
    private val values = ArrayList<PlayerSpecificValue<*>>()
    private val overrides = Object2ObjectOpenHashMap<UUID, Overrides>()

    /**
     * Registers a new value with the given [base] value.
     *
     * All values must be registered before any overrides are set,
     * typically as properties of the visual itself:
     * ```
     * val title: PlayerSpecificValue<Component> = this.data.register(CommonComponents.EMPTY)
     * ```
     *
     * @param base The initial base value.
     * @return The registered value.
     */
    public fun <T: Any> register(base: T): PlayerSpecificValue<T> {
        check(this.overrides.isEmpty()) { "Cannot register values after player-specific overrides have been set" }
        check(this.values.size < Int.SIZE_BITS) { "Cannot register more than ${Int.SIZE_BITS} values" }
        val value = PlayerSpecificValue(this, this.values.size, base)
        this.values.add(value)
        return value
    }

    /**
     * Gets the mask of base values which have changed since this
     * was last called, and marks them as no longer dirty.
     *
     * This **must** be called exactly once per tick, before iterating
     * over observers; the returned mask is then passed to [clean] for
     * each observing player.
     *
     * @return The mask of changed base values.
     */
    public fun clean(): Int {
        var dirty = 0
        for (value in this.values) {
            if (value.baseDirty) {
                value.baseDirty = false
                dirty = dirty or value.bit
            }
        }
        return dirty
    }

    /**
     * Gets the mask of values which have changed for the given [player]
     * since this was last called, and marks them as no longer dirty.
     *
     * The given [base] mask, from [clean], is included in the
     * result, minus any values which the player overrides, since a
     * change to a base value must not overwrite an override.
     *
     * @param player The uuid of the player.
     * @param base The mask returned by [clean].
     * @return The mask of changed values for the given [player].
     */
    public fun clean(player: UUID, base: Int): Int {
        val overrides = this.overrides[player] ?: return base
        val dirty = (base and overrides.overridden.inv()) or overrides.dirty
        overrides.dirty = 0
        if (overrides.overridden == 0) {
            this.overrides.remove(player)
        }
        return dirty
    }

    /**
     * Marks all the given [player]'s values as no longer dirty.
     *
     * This should be called when a visual sends the given player its
     * full state, for example, in [VirtualVisual.sendSpawnPackets],
     * to avoid immediately sending them a redundant update.
     *
     * @param player The uuid of the player.
     */
    public fun clean(player: UUID) {
        this.overrides[player]?.dirty = 0
    }

    /**
     * Removes all of the given [player]'s overrides, so that they
     * are shown the base values again.
     *
     * @param player The uuid of the player.
     */
    public fun setToBase(player: UUID) {
        if (!this.overrides.containsKey(player)) {
            return
        }
        for (value in this.values) {
            value.setToBase(player)
        }
    }

    /**
     * Discards all state held for the given [player], without
     * marking anything dirty.
     *
     * Unlike [setToBase] this does *not* update anything currently
     * being shown to the player; it is intended for evicting a player
     * who is no longer expected to observe this visual.
     *
     * @param player The uuid of the player.
     */
    public fun remove(player: UUID) {
        this.overrides.remove(player)
    }

    /**
     * Checks whether the given [player] overrides the given [value].
     *
     * @param player The uuid of the player.
     * @param value The value to check.
     * @return Whether the player has an override.
     */
    public fun isOverridden(player: UUID, value: PlayerSpecificValue<*>): Boolean {
        val overrides = this.overrides[player] ?: return false
        return overrides.overridden and value.bit != 0
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T: Any> getOverride(player: UUID, value: PlayerSpecificValue<T>): T? {
        return this.overrides[player]?.values?.get(value.index) as T?
    }

    internal fun <T: Any> setOverride(player: UUID, value: PlayerSpecificValue<T>, override: T) {
        val overrides = this.overrides.getOrPut(player) { Overrides(this.values.size) }
        overrides.values[value.index] = override
        overrides.overridden = overrides.overridden or value.bit
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <T: Any> removeOverride(player: UUID, value: PlayerSpecificValue<T>): T? {
        val overrides = this.overrides[player] ?: return null
        val previous = overrides.values[value.index] as? T ?: return null
        overrides.values[value.index] = null
        overrides.overridden = overrides.overridden and value.bit.inv()
        return previous
    }

    internal fun markDirty(player: UUID, bit: Int) {
        val overrides = this.overrides[player] ?: return
        overrides.dirty = overrides.dirty or bit
    }

    private class Overrides(size: Int) {
        val values: Array<Any?> = arrayOfNulls(size)

        var overridden: Int = 0
        var dirty: Int = 0
    }
}
