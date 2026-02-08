/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.data

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.ints.IntOpenHashSet
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.visuals.entity.data.PlayerSpecificEntityData.Companion.mergeEntityData
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.EntityType
import java.util.*
import kotlin.collections.iterator

/**
 * This class works similar to [SynchedEntityData] but
 * allows for player-specific overrides.
 *
 * The class contains a base set of entity data entries
 * and a collection of overridden entity data entries
 * specified on a per-player basis. If there is no
 * override for an entry then the base entity data
 * should be used instead, see [mergeEntityData].
 */
public class PlayerSpecificEntityData(type: EntityType<*>) {
    private val overrides = Object2ObjectOpenHashMap<UUID, Int2ObjectOpenHashMap<EntityDataEntry<*>>>()
    private val dirty = ObjectOpenHashSet<UUID>()

    private val base = SimpleEntityData(type)

    public fun <T: Any> get(observer: UUID, accessor: EntityDataAccessor<T>): T? {
        return this.getEntry(observer, accessor)?.value
    }

    public fun <T: Any> getEntry(observer: UUID, accessor: EntityDataAccessor<T>): EntityDataEntry<T>? {
        return this.getOverriddenEntry(observer, accessor) ?: this.getBaseEntry(accessor)
    }

    public fun <T: Any> set(observer: UUID, accessor: EntityDataAccessor<T>, value: T, force: Boolean = false): Boolean {
        val entry = this.getOrCreateOverriddenEntry(observer, accessor) ?: return false
        if (entry.update(value, force)) {
            this.dirty.add(observer)
            return true
        }
        return false
    }

    public fun <T: Any> setToBase(observer: UUID, accessor: EntityDataAccessor<T>): Boolean {
        val base = this.getBaseEntry(accessor) ?: return false
        if (this.isOverridden(observer, accessor)) {
            this.set(observer, accessor, base.value, true)
            return true
        }
        return false
    }

    public fun <T: Any> modifyEntry(observer: UUID, accessor: EntityDataAccessor<T>, force: Boolean, modifier: (T) -> T): Boolean {
        val entry = this.getOrCreateOverriddenEntry(observer, accessor) ?: return false
        if (entry.update(modifier.invoke(entry.value), force)) {
            this.dirty.add(observer)
            return true
        }
        return false
    }

    public fun <T: Any> modifyEntry(accessor: EntityDataAccessor<T>, force: Boolean = false, modifier: (T) -> T): Boolean {
        if (this.base.modify(accessor, force, modifier)) {
            for ((observer, overrides) in this.overrides) {
                @Suppress("UNCHECKED_CAST")
                val entry = overrides.get(accessor.id) as EntityDataEntry<T>? ?: continue
                if (entry.update(modifier.invoke(entry.value), force)) {
                    this.dirty.add(observer)
                }
            }
            return true
        }
        return false
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

    public fun getDirtyEntries(observer: UUID, base: List<SynchedEntityData.DataValue<*>>): List<SynchedEntityData.DataValue<*>> {
        val overrides = this.overrides[observer] ?: return base
        if (!this.dirty.remove(observer)) {
            return base.filter { value -> !overrides.containsKey(value.id) }
        }

        val dirty = Int2ObjectOpenHashMap<SynchedEntityData.DataValue<*>>()
        for (value in base) {
            dirty.put(value.id, value)
        }

        val entries = overrides.int2ObjectEntrySet().fastIterator()
        for (pair in entries) {
            val entry = pair.value
            if (!entry.clean()) {
                dirty.remove(pair.intKey)
                continue
            }
            dirty.put(pair.intKey, entry.serialize())

            val base = this.base.getEntry(pair.intKey) ?: continue
            if (base.isValueEqualTo(entry.value)) {
                entries.remove()
            }
        }
        return dirty.values.toList()
    }

    public fun getChangedEntries(observer: UUID, base: List<SynchedEntityData.DataValue<*>>): List<SynchedEntityData.DataValue<*>> {
        val entries = this.overrides[observer]?.values ?: return base
        val changed = Int2ObjectOpenHashMap<SynchedEntityData.DataValue<*>>()
        for (value in base) {
            changed.put(value.id, value)
        }
        for (entry in entries) {
            if (!entry.unchanged()) {
                val value = entry.serialize()
                changed.put(value.id, value)
            } else {
                changed.remove(entry.id)
            }
        }
        return changed.values.toList()
    }

    public fun <T: Any> getBase(accessor: EntityDataAccessor<T>): T? {
        return this.base.get(accessor)
    }

    public fun <T: Any> setBase(accessor: EntityDataAccessor<T>, value: T) {
        this.base.set(accessor, value)
    }

    public fun <T: Any> getBaseEntry(accessor: EntityDataAccessor<T>): EntityDataEntry<T>? {
        return this.base.getEntry(accessor)
    }

    public fun isBaseDirty(accessor: EntityDataAccessor<*>): Boolean {
        return this.base.isDirty(accessor)
    }

    public fun getDirtyBaseEntries(): List<SynchedEntityData.DataValue<*>> {
        return this.base.getDirtyEntries() ?: listOf()
    }

    public fun getChangedBaseEntries(): List<SynchedEntityData.DataValue<*>> {
        return this.base.getChangedEntries() ?: listOf()
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> getOverriddenEntry(observer: UUID, accessor: EntityDataAccessor<T>): EntityDataEntry<T>? {
        return (this.overrides[observer]?.get(accessor.id) as? EntityDataEntry<T>)
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T: Any> getOrCreateOverriddenEntry(observer: UUID, accessor: EntityDataAccessor<T>): EntityDataEntry<T>? {
        val base = this.getBaseEntry(accessor) ?: return null
        val overrides = this.overrides.getOrPut(observer, ::Int2ObjectOpenHashMap)
        return overrides.getOrPut(accessor.id, base::copy) as EntityDataEntry<T>
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