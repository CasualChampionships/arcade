/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.data

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import java.util.ArrayList

/**
 * This class works exactly like [SynchedEntityData].
 */
public class SimpleEntityData {
    private val entries: Array<EntityDataEntry<*>>

    public constructor(type: EntityType<*>) {
        val examples = PolymerEntityUtils.getDefaultSynchedEntityData(type)
        this.entries = Array(examples.size) { i ->
            val example = examples[i]
            @Suppress("UNCHECKED_CAST")
            EntityDataEntry(example.accessor as EntityDataAccessor<in Any>, example.value)
        }
    }

    public fun <T: Any> get(accessor: EntityDataAccessor<T>): T? {
        return this.getEntry(accessor)?.value
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T: Any> getEntry(accessor: EntityDataAccessor<T>): EntityDataEntry<T>? {
        val entry = this.getEntry(accessor.id) ?: return null
        return if (entry.isOf(accessor)) (entry as EntityDataEntry<T>) else null
    }

    public fun getEntry(id: Int): EntityDataEntry<*>? {
        return this.entries.getOrNull(id)
    }

    public fun <T: Any> set(accessor: EntityDataAccessor<T>, value: T, force: Boolean = false): Boolean {
        val entry = this.getEntry(accessor) ?: return false
        return entry.update(value, force)
    }

    public fun <T: Any> modify(accessor: EntityDataAccessor<T>, force: Boolean = false, modifier: (T) -> T): Boolean {
        val entry = this.getEntry(accessor) ?: return false
        return entry.update(modifier.invoke(entry.value), force)
    }

    public fun isDirty(accessor: EntityDataAccessor<*>): Boolean {
        val entry = this.getEntry(accessor)
        return entry != null && entry.dirty
    }

    public fun getDirtyEntries(): List<SynchedEntityData.DataValue<*>>? {
        var dirty: MutableList<SynchedEntityData.DataValue<*>>? = null
        for (entry in this.entries) {
            if (entry.clean()) {
                if (dirty == null) {
                    dirty = ArrayList()
                }
                dirty.add(entry.serialize())
            }
        }
        return dirty
    }

    public fun getChangedEntries(): List<SynchedEntityData.DataValue<*>>? {
        var changed: MutableList<SynchedEntityData.DataValue<*>>? = null
        for (entry in this.entries) {
            if (!entry.unchanged()) {
                if (changed == null) {
                    changed = ArrayList()
                }
                changed.add(entry.serialize())
            }
        }
        return changed
    }
}