/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.collision

import net.casual.arcade.networking.observer.Observer
import net.casual.arcade.networking.observer.tracker.ObserverTracker
import net.casual.arcade.networking.packet.PacketSender
import net.casual.arcade.utils.ClientboundSetPassengersPacket
import net.casual.arcade.virtual.entity.SimpleParentVirtualEntity
import net.casual.arcade.virtual.entity.SimpleVirtualEntity
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.display.SimpleVirtualTextDisplay
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.Attributes

public class CollisionCubeVirtualEntity(
    attachment: VirtualEntityAttachment,
    tracker: ObserverTracker
): SimpleParentVirtualEntity(attachment, tracker) {
    private val vehicle = this.attachWithParentObservers(::SimpleVirtualTextDisplay)
    private val shulker = this.attachWithParentObservers(SimpleVirtualEntity.typed(EntityTypes.SHULKER))

    private var scale: Double = 1.0

    init {
        this.vehicle.setInvisible(true)
        this.shulker.setInvisible(true)
    }

    public fun setScale(scale: Double) {
        this.scale = scale

        this.observers.broadcast(ClientboundUpdateAttributesPacket(this.shulker.id, listOf(this.getScaleAttribute())))
    }

    override fun sendSpawnPackets(observer: Observer, sender: PacketSender) {
        super.sendSpawnPackets(observer, sender)

        sender.send(ClientboundUpdateAttributesPacket(this.shulker.id, listOf(this.getScaleAttribute())))
        sender.send(ClientboundSetPassengersPacket(this.vehicle.id, intArrayOf(this.shulker.id)))
    }

    private fun getScaleAttribute(): AttributeInstance {
        SCALE_ATTRIBUTE.baseValue = this.scale
        return SCALE_ATTRIBUTE
    }

    private companion object {
        val SCALE_ATTRIBUTE = AttributeInstance(Attributes.SCALE) {}
    }
}