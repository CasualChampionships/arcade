/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.data

import eu.pb4.polymer.common.impl.entity.InternalEntityHelpers
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import java.util.*

public class PlayerSpecificEntityData {
    private val overrides = Object2ObjectOpenHashMap<UUID, Int2ObjectOpenHashMap<Entry<*>>>()
    private val dirty = ObjectOpenHashSet<UUID>()

    private val base: Array<Entry<*>>

    public constructor(type: EntityType<*>) {
        @Suppress("UnstableApiUsage")
        val examples = InternalEntityHelpers.getExampleTrackedDataOfEntityType(type)
        this.base = Array(examples.size) { i ->
            val example = examples[i]
            Entry(example.accessor as EntityDataAccessor<in Any>, example.value)
        }
    }

    public fun <T> get(observer: UUID, accessor: EntityDataAccessor<T>): T? {
        return this.getEntry(observer, accessor)?.value
    }

    public fun <T> getEntry(observer: UUID, accessor: EntityDataAccessor<T>): Entry<T>? {
        return this.getOverriddenEntry(observer, accessor) ?: this.getBaseEntry(accessor)
    }

    public fun isDirty(observer: UUID, accessor: EntityDataAccessor<*>): Boolean {
        val entry = this.getEntry(observer, accessor)
        return entry != null && entry.dirty
    }

    public fun remove(observer: UUID) {
        this.overrides.remove(observer)
        this.dirty.remove(observer)
    }

    public fun isOverridden(observer: UUID, accessor: EntityDataAccessor<*>): Boolean {
        return this.overrides[observer]?.containsKey(accessor.id) ?: false
    }

    public fun getDirtyObservers(): Set<UUID> {
        return this.dirty
    }

    public fun <T> set(observer: UUID, accessor: EntityDataAccessor<T>, value: T, force: Boolean = false) {
        val entry = this.getOrCreateOverriddenEntry(observer, accessor) ?: return
        if (force || value != entry.value) {
            entry.value = value
            entry.dirty = true
            this.dirty.add(observer)
        }
    }

    public fun <T> setToBase(observer: UUID, accessor: EntityDataAccessor<T>) {
        val base = this.getBaseEntry(accessor) ?: return
        if (this.isOverridden(observer, accessor)) {
            this.set(observer, accessor, base.value, true)
        }
    }

    public fun <T> modifyEntry(accessor: EntityDataAccessor<T>, force: Boolean = false, modifier: (T) -> T) {
        val base = this.getBaseEntry(accessor) ?: return
        base.value = modifier.invoke(base.value)
        base.dirty = true
        for ((observer, overrides) in this.overrides) {
            @Suppress("UNCHECKED_CAST")
            val entry = overrides.get(accessor.id) as Entry<T>? ?: continue
            val updated = modifier.invoke(entry.value)
            if (force || updated != entry.value) {
                entry.value = updated
                entry.dirty = true
                this.dirty.add(observer)
            }
        }
    }

    public fun getDirtyEntries(observer: UUID): List<SynchedEntityData.DataValue<*>>? {
        var dirty: MutableList<SynchedEntityData.DataValue<*>>? = null
        if (this.dirty.remove(observer)) {
            val entries = this.overrides[observer]?.int2ObjectEntrySet()?.fastIterator() ?: return null
            for (pair in entries) {
                val entry = pair.value
                if (!entry.dirty) {
                    continue
                }
                entry.dirty = false
                if (dirty == null) {
                    dirty = ArrayList()
                }
                dirty.add(entry.serialize())

                val base = this.base.getOrNull(pair.intKey) ?: continue
                if (base.value == entry.value) {
                    entries.remove()
                }
            }
        }
        return dirty
    }

    public fun getChangedEntries(observer: UUID): List<SynchedEntityData.DataValue<*>>? {
        var changed: MutableList<SynchedEntityData.DataValue<*>>? = null
        val entries = this.overrides[observer]?.values ?: return null
        for (entry in entries) {
            if (!entry.unchanged()) {
                if (changed == null) {
                    changed = ArrayList()
                }
                changed.add(entry.serialize())
            }
        }
        return changed
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T> getBaseEntry(accessor: EntityDataAccessor<T>): Entry<T>? {
        val entry = this.base.getOrElse(accessor.id) {
            return null
        }
        return if (entry.isOf(accessor)) (entry as Entry<T>) else null
    }

    public fun isBaseDirty(accessor: EntityDataAccessor<*>): Boolean {
        val entry = this.getBaseEntry(accessor)
        return entry != null && entry.dirty
    }

    public fun getDirtyBaseEntries(): List<SynchedEntityData.DataValue<*>>? {
        var dirty: MutableList<SynchedEntityData.DataValue<*>>? = null
        for (entry in this.base) {
            if (entry.dirty) {
                entry.dirty = false
                if (dirty == null) {
                    dirty = ArrayList()
                }
                dirty.add(entry.serialize())
            }
        }
        return dirty
    }

    public fun getChangedBaseEntries(): List<SynchedEntityData.DataValue<*>>? {
        var changed: MutableList<SynchedEntityData.DataValue<*>>? = null
        for (entry in this.base) {
            if (!entry.unchanged()) {
                if (changed == null) {
                    changed = ArrayList()
                }
                changed.add(entry.serialize())
            }
        }
        return changed
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getOverriddenEntry(observer: UUID, accessor: EntityDataAccessor<T>): Entry<T>? {
        return (this.overrides[observer]?.get(accessor.id) as? Entry<T>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T> getOrCreateOverriddenEntry(observer: UUID, accessor: EntityDataAccessor<T>): Entry<T>? {
        val base = this.getBaseEntry(accessor) ?: return null
        val overrides = this.overrides.getOrPut(observer, ::Int2ObjectOpenHashMap)
        return overrides.getOrPut(accessor.id, base::copy) as Entry<T>
    }

    public class Entry<T>(
        private val accessor: EntityDataAccessor<T>,
        private var initialValue: T
    ) {
        public var value: T = this.initialValue
        public var dirty: Boolean = false

        public fun unchanged(): Boolean {
            return this.initialValue == this.value
        }

        public fun isOf(accessor: EntityDataAccessor<*>): Boolean {
            return this.accessor == accessor
        }

        public fun serialize(): SynchedEntityData.DataValue<T> {
            return SynchedEntityData.DataValue.create(this.accessor, this.value)
        }

        public fun copy(): Entry<T> {
            val copy = Entry(this.accessor, this.initialValue)
            copy.value = this.value
            copy.dirty = true
            return copy
        }
    }

    public companion object {
        public fun mergeEntityData(
            base: List<SynchedEntityData.DataValue<*>>?,
            overridden: List<SynchedEntityData.DataValue<*>>?
        ): List<SynchedEntityData.DataValue<*>>? {
            if (base == null) {
                return overridden
            }
            if (overridden == null) {
                return base
            }
            val ignore = IntOpenHashSet()
            overridden.forEach { entry -> ignore.add(entry.id) }
            val combined = ArrayList(overridden)
            base.filterTo(combined) { entry -> !ignore.contains(entry.id) }
            return combined
        }
    }
}