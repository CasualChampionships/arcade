/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.virtual.entity.VirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.data.SimpleEntityData
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.location.VirtualRotation
import net.casual.arcade.virtual.entity.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors
import net.casual.arcade.virtual.entity.utils.EntityDataSharedFlags
import net.casual.arcade.virtual.entity.utils.location
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import java.util.*

public class NametagHeightVirtualEntity(
    override val attachment: VirtualEntityAttachment,
    override val observers: ObserverTracker,
    private val height: NametagHeight,
    private val handler: VirtualEntity.InteractionHandler = PassthroughInteractionHandler
): VirtualEntity {
    override val id: Int = VirtualEntity.getNextEntityId()
    override val uuid: UUID = UUID.randomUUID()
    override var position: VirtualPosition = VirtualPosition.DEFAULT
    override var rotation: VirtualRotation = VirtualRotation.DEFAULT

    override fun tick() {

    }

    override fun sendSpawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        val location = this.location()
        val (x, y, z) = location.position
        consumer.invoke(ClientboundAddEntityPacket(
            this.id, this.uuid, x, y, z, 0.0F, 0.0F, EntityType.ARMOR_STAND, 0, Vec3.ZERO, 0.0
        ))
        consumer.invoke(ClientboundSetEntityDataPacket(this.id, changed))
        val attribute = this.height.attribute
        if (attribute != null) {
            consumer.invoke(ClientboundUpdateAttributesPacket(this.id, listOf(attribute)))
        }
    }

    override fun sendDespawnPackets(observer: ServerPlayer, consumer: (Packet<*>) -> Unit) {
        consumer.invoke(ClientboundRemoveEntitiesPacket(this.id))
    }

    override fun getInteractionHandler(player: ServerPlayer): VirtualEntity.InteractionHandler {
        return this.handler
    }

    private companion object {
        val changed by lazy {
            val data = SimpleEntityData(EntityType.ARMOR_STAND)
            data.set(EntityDataAccessors.SILENT, true)
            data.set(EntityDataAccessors.NO_GRAVITY, true)
            data.set(EntityDataAccessors.SHARED_FLAGS, EntityDataSharedFlags.INVISIBLE.toByte())
            data.set(EntityDataAccessors.LivingEntity.HEALTH, 0.0F)
            data.getChangedEntries()!!
        }
    }
}