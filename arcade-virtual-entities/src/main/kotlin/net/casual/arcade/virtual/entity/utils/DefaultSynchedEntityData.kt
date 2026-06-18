/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import io.netty.util.internal.shaded.org.jctools.util.UnsafeAccess
import net.casual.arcade.utils.compat.PolymerCompatLayer
import net.casual.arcade.virtual.entity.mixins.hack.EntityInvoker
import net.casual.arcade.virtual.entity.mixins.hack.SynchedEntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.Pose
import java.lang.reflect.ParameterizedType
import java.util.Optional

internal object DefaultSynchedEntityData {
    private val types by lazy(::createTypeMap)

    private val data = HashMap<EntityType<*>, Array<SynchedEntityData.DataItem<*>>>()

    fun get(type: EntityType<*>): Array<SynchedEntityData.DataItem<*>> {
        return PolymerCompatLayer.getDefaultSynchedEntityDataOrNull(type)
            ?: this.data.computeIfAbsent(type, this::createForType)
    }

    private fun createForType(type: EntityType<*>): Array<SynchedEntityData.DataItem<*>> {
        val clazz = this.types[type]
            ?: throw IllegalArgumentException("Failed to create synched data for type $type, unknown class")
        return this.createForClass(clazz)
    }

    private fun createForClass(type: Class<*>): Array<SynchedEntityData.DataItem<*>> {
        val entity = UnsafeAccess.UNSAFE.allocateInstance(type) as Entity
        val builder = SynchedEntityData.Builder(entity)
        builder.define(EntityDataAccessors.SHARED_FLAGS, 0)
        builder.define(EntityDataAccessors.AIR_SUPPLY, entity.maxAirSupply)
        builder.define(EntityDataAccessors.CUSTOM_NAME_VISIBLE, false)
        builder.define(EntityDataAccessors.CUSTOM_NAME, Optional.empty())
        builder.define(EntityDataAccessors.SILENT, false)
        builder.define(EntityDataAccessors.NO_GRAVITY, false)
        builder.define(EntityDataAccessors.POSE, Pose.STANDING)
        builder.define(EntityDataAccessors.TICKS_FROZEN, 0)
        (entity as EntityInvoker).arcade_defineSynchedData(builder)
        return (builder.build() as SynchedEntityDataAccessor).arcade_getItemsById()
    }

    private fun createTypeMap(): HashMap<EntityType<*>, Class<*>?> {
        val map = HashMap<EntityType<*>, Class<*>?>()
        for (field in EntityTypes::class.java.declaredFields) {
            if (field.type != EntityType::class.java) {
                continue
            }

            field.setAccessible(true)
            val entityType = field.get(null) as EntityType<*>
            val type = field.genericType as ParameterizedType
            map[entityType] = type.actualTypeArguments[0] as Class<*>?
        }
        return map
    }
}