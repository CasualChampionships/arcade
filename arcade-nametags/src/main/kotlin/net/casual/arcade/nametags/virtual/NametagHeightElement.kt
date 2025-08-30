/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement
import eu.pb4.polymer.virtualentity.api.tracker.EntityTrackedData
import eu.pb4.polymer.virtualentity.api.tracker.SimpleDataTracker
import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.nametags.mixins.LivingEntityAccessor
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType.ARMOR_STAND
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer

public class NametagHeightElement(
    private val entity: Entity,
    public val height: NametagHeight
): AbstractElement() {
    private val uuid: UUID = UUID.randomUUID()
    public val id: Int = VirtualEntityUtils.requestEntityId()

    override fun tick() {

    }

    override fun startWatching(player: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {
        val pos = this.entity.position()
        consumer.accept(ClientboundAddEntityPacket(
            this.id, this.uuid, pos.x, pos.y, pos.z, 0.0F, 0.0F, ARMOR_STAND, 0, Vec3.ZERO, 0.0
        ))
        consumer.accept(ClientboundSetEntityDataPacket(this.id, tracked))
        val attribute = this.height.attribute
        if (attribute != null) {
            consumer.accept(ClientboundUpdateAttributesPacket(this.id, listOf(attribute)))
        }
    }

    override fun stopWatching(player: ServerPlayer, consumer: Consumer<Packet<ClientGamePacketListener>>) {

    }

    override fun getEntityIds(): IntList {
        return IntList.of(this.id)
    }

    override fun notifyMove(oldPos: Vec3, currentPos: Vec3, delta: Vec3) {

    }

    public companion object {
        private val tracked by lazy {
            val tracker = SimpleDataTracker(ARMOR_STAND)
            tracker.set(EntityTrackedData.SILENT, true)
            tracker.set(EntityTrackedData.NO_GRAVITY, true)
            tracker.set(EntityTrackedData.FLAGS, (1 shl EntityTrackedData.INVISIBLE_FLAG_INDEX).toByte())
            // We mark the armor stand as 'dead' to prevent projectiles from hitting it
            tracker.set(LivingEntityAccessor.getHealthDataAccessor(), 0.0F)
            tracker.changedEntries!!
        }
    }
}