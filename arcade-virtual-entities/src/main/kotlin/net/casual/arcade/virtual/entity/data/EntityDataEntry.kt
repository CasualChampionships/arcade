package net.casual.arcade.virtual.entity.data

import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData

public class EntityDataEntry<T: Any>(
    private val accessor: EntityDataAccessor<T>,
    private var initialValue: T
) {
    public var value: T = this.initialValue
        private set
    public var dirty: Boolean = false
        private set

    public fun update(updated: T, force: Boolean = false): Boolean {
        if (force || this.value != updated) {
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
        return this.initialValue == this.value
    }

    public fun isOf(accessor: EntityDataAccessor<*>): Boolean {
        return this.accessor == accessor
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