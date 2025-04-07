/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.utils

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket
import eu.pb4.polymer.core.impl.interfaces.PossiblyInitialPacket
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData
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
    return this.modify(player, EntityTrackedData.FLAGS, modifier)
}

public inline fun ClientboundSetEntityDataPacket.modifyFrozenTicks(
    player: ServerPlayer,
    modifier: (observee: Entity, observer: ServerPlayer, ticks: Int) -> Int
): ClientboundSetEntityDataPacket {
    return this.modify(player, EntityTrackedData.FROZEN_TICKS, modifier)
}

public inline fun ClientboundSetEntityDataPacket.modifyPose(
    player: ServerPlayer,
    modifier: (observee: Entity, observer: ServerPlayer, pose: Pose) -> Pose
): ClientboundSetEntityDataPacket {
    return this.modify(player, EntityTrackedData.POSE, modifier)
}

public inline fun <reified T: Any> ClientboundSetEntityDataPacket.modify(
    player: ServerPlayer,
    accessor: EntityDataAccessor<T>,
    modifier: (observee: Entity, observer: ServerPlayer, data: T) -> T
): ClientboundSetEntityDataPacket {
    val observee = player.serverLevel().getEntity(this.id) ?: return this

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
    if (!changed) {
        @Suppress("CAST_NEVER_SUCCEEDS")
        val isInitial = (this as PossiblyInitialPacket).`polymer$getInitial`()
        if (!isInitial) {
            return this
        }

        val value = observee.entityData.get(accessor)
        val modified = modifier.invoke(observee, player, value)
        data.add(DataValue.create(accessor, modified))
    }

    val replacement = ClientboundSetEntityDataPacket(this.id, data)
    // For polymer compatability
    EntityAttachedPacket.set(replacement, observee)
    return replacement
}