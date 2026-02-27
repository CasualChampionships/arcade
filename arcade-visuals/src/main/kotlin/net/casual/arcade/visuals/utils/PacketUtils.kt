/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.utils

import net.casual.arcade.utils.compat.PolymerCompatLayer
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.Pose

public inline fun ClientboundSetEntityDataPacket.modifySharedFlags(
    player: ServerPlayer,
    modifier: (observee: Entity, observer: ServerPlayer, flags: Byte) -> Byte
): ClientboundSetEntityDataPacket {
    return this.modify(player, EntityDataAccessors.SHARED_FLAGS, modifier)
}

public inline fun ClientboundSetEntityDataPacket.modifyFrozenTicks(
    player: ServerPlayer,
    modifier: (observee: Entity, observer: ServerPlayer, ticks: Int) -> Int
): ClientboundSetEntityDataPacket {
    return this.modify(player, EntityDataAccessors.TICKS_FROZEN, modifier)
}

public inline fun ClientboundSetEntityDataPacket.modifyPose(
    player: ServerPlayer,
    modifier: (observee: Entity, observer: ServerPlayer, pose: Pose) -> Pose
): ClientboundSetEntityDataPacket {
    return this.modify(player, EntityDataAccessors.POSE, modifier)
}

public inline fun <reified T: Any> ClientboundSetEntityDataPacket.modify(
    player: ServerPlayer,
    accessor: EntityDataAccessor<T>,
    modifier: (observee: Entity, observer: ServerPlayer, data: T) -> T
): ClientboundSetEntityDataPacket {
    val observee = player.level().getEntity(this.id) ?: return this

    val items = this.packedItems
    val data = ArrayList<DataValue<*>>()
    var changed = false
    for (item in items) {
        if (item.id == accessor.id) {
            val value = item.value as T
            val modified = modifier.invoke(observee, player, value)
            data.add(DataValue.create(accessor, modified))
            changed = true
        } else {
            data.add(item)
        }
    }

    val isInitial = PolymerCompatLayer.isInitial(this)
    if (!changed) {
        if (!isInitial) {
            return this
        }

        val value = observee.entityData.get(accessor)
        val modified = modifier.invoke(observee, player, value)
        data.add(DataValue.create(accessor, modified))
    }

    val replacement = ClientboundSetEntityDataPacket(this.id, data)
    if (isInitial) {
        PolymerCompatLayer.setInitial(replacement)
    }
    PolymerCompatLayer.setEntityContext(replacement, observee)
    return replacement
}

public inline fun ClientboundSetEntityDataPacket.modifyVirtualSharedFlags(
    player: ServerPlayer,
    modifier: (observee: Int, observer: ServerPlayer, flags: Byte) -> Byte
): ClientboundSetEntityDataPacket {
    return this.modifyVirtual(player, EntityDataAccessors.SHARED_FLAGS, modifier)
}

public inline fun <reified T: Any> ClientboundSetEntityDataPacket.modifyVirtual(
    player: ServerPlayer,
    accessor: EntityDataAccessor<T>,
    modifier: (observee: Int, observer: ServerPlayer, data: T) -> T
): ClientboundSetEntityDataPacket {
    val items = this.packedItems
    val data = ArrayList<DataValue<*>>()
    for (item in items) {
        if (item.id == accessor.id) {
            val value = item.value as T
            val modified = modifier.invoke(this.id, player, value)
            data.add(DataValue.create(accessor, modified))
        } else {
            data.add(item)
        }
    }

    return ClientboundSetEntityDataPacket(this.id, data)
}