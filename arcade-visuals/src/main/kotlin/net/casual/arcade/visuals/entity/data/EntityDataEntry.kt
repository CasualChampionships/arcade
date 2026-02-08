/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.data

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.item.ItemStack

public class EntityDataEntry<T: Any>(
    private val accessor: EntityDataAccessor<T>,
    private var initialValue: T
) {
    public var value: T = this.initialValue
        private set
    public var dirty: Boolean = false
        private set

    public val id: Int
        get() = this.accessor.id

    public fun update(updated: T, force: Boolean = false): Boolean {
        if (force || !this.isValueEqualTo(updated)) {
            this.value = updated
            this.dirty = true
            return true
        }
        return false
    }

    public fun clean(): Boolean {
        val wasDirty = this.dirty
        this.dirty = false
        return wasDirty
    }

    public fun unchanged(): Boolean {
        return this.isValueEqualTo(this.initialValue)
    }

    public fun isOf(accessor: EntityDataAccessor<*>): Boolean {
        return this.accessor == accessor
    }

    public fun isValueEqualTo(other: Any?): Boolean {
        val current = this.value
        if (current is ItemStack && other is ItemStack) {
            return ItemStack.isSameItemSameComponents(current, other)
        }
        return current == other
    }

    public fun serialize(): SynchedEntityData.DataValue<T> {
        return SynchedEntityData.DataValue.create(this.accessor, this.value)
    }

    public fun copy(): EntityDataEntry<T> {
        val copy = EntityDataEntry(this.accessor, this.initialValue)
        copy.value = this.value
        copy.dirty = true
        return copy
    }
}