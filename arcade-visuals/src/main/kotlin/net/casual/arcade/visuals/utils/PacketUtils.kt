/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.utils

import eu.pb4.polymer.core.impl.interfaces.EntityAttachedPacket
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public inline fun ClientboundSetEntityDataPacket.modifySharedFlags(
    player: ServerPlayer,
    modifier: (observee: Entity, observer: ServerPlayer, flags: Byte) -> Byte
): ClientboundSetEntityDataPacket {
    val observee = player.serverLevel().getEntity(this.id) ?: return this

    val items = this.packedItems
    val data = ArrayList<DataValue<*>>()
    var changed = false
    for (item in items) {
        if (item.id == EntityTrackedData.FLAGS.id) {
            val flags = item.value as Byte
            val modified = modifier.invoke(observee, player, flags)
            data.add(DataValue.create(EntityTrackedData.FLAGS, modified))
            changed = true
        } else {
            data.add(item)
        }
    }
    if (!changed) {
        return this
    }

    val replacement = ClientboundSetEntityDataPacket(this.id, data)
    // For polymer compatability
    EntityAttachedPacket.set(replacement, observee)
    return replacement
}