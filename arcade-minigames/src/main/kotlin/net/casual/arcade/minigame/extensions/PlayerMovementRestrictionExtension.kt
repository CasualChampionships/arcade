/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerAttributeUpdatedEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.SerializableExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.utils.getExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ClientboundSetPassengersPacket
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.virtual.entity.SimpleVirtualEntity
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.EntityAttachmentAnchor
import net.casual.arcade.virtual.entity.display.SimpleVirtualTextDisplay
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket.AttributeSnapshot
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import net.minecraft.world.phys.Vec3
import org.jetbrains.annotations.VisibleForTesting

public class PlayerMovementRestrictionExtension(player: ServerPlayer): PlayerExtension(player), SerializableExtension {
    private var attachment: SimpleVirtualEntityAttachment? = null
    private var position: Vec3 = Vec3.ZERO
    private var persist: Boolean = false

    public val hasRestrictedMovement: Boolean
        get() = this.attachment != null

    override fun id(): Identifier {
        return arcade("movement_restriction")
    }

    override fun serialize(output: ValueOutput) {
        if (this.persist && this.hasRestrictedMovement) {
            output.store("position", Vec3.CODEC, this.position)
            output.putBoolean("persist", this.persist)
        }
    }

    override fun deserialize(input: ValueInput) {
        this.position = input.read("position", Vec3.CODEC).orElse(Vec3.ZERO)
        this.persist = input.getBooleanOr("persist", false)
        if (this.persist) {
            GlobalTickedScheduler.later {
                this.restrictMovement(true)
            }
        }
    }

    private fun restrictMovement(persist: Boolean) {
        this.persist = persist
        this.position = this.player.position()
        if (this.attachment != null) {
            return
        }

        // TODO: We can rework this to have a MoveableHitbox entity
        val dimensions = this.player.getDimensions(this.player.pose)
        val attachment = SimpleVirtualEntityAttachment(EntityAttachmentAnchor(this.player))
        val packets = ArrayList<Packet<*>>(12)
        for (direction in Direction.entries) {
            val vehicle = attachment.attachWithParentObservers(::SimpleVirtualTextDisplay)
            val element = attachment.attachWithParentObservers(SimpleVirtualEntity.typed(EntityType.SHULKER))
            vehicle.setInvisible(true)
            if (!DEBUG) {
                element.setInvisible(true)
            }

            val horizontalSize = dimensions.width
            val verticalSize = dimensions.height
            val unit = direction.unitVec3

            val offsetX = unit.x * (horizontalSize / 2 + SMALLEST_SCALE / 2)
            val offsetY = ((unit.y + 1) / 2) * unit.y * verticalSize + SMALLEST_SCALE * unit.y -
                    (unit.y - 1) * (unit.y + 1) * verticalSize / 2
            val offsetZ = unit.z * (horizontalSize / 2 + SMALLEST_SCALE / 2)

            vehicle.position += Vec3(offsetX, offsetY, offsetZ)
            packets.add(ClientboundUpdateAttributesPacket(element.id, listOf(SMALLEST_SCALE_ATTRIBUTE)))
            packets.add(ClientboundSetPassengersPacket(vehicle.id, intArrayOf(element.id)))
        }
        attachment.startObservingAttached(this.player)
        for (packet in packets) {
            this.player.connection.send(packet)
        }
        this.player.connection.send(ClientboundUpdateAttributesPacket(this.player.id, listOf(SMALLEST_JUMP_ATTRIBUTE)))
        this.player.teleportTo(this.player.locationWithLevel)
        this.attachment = attachment
    }

    private fun unrestrictMovement() {
        val attachment = this.attachment ?: return
        attachment.stopObservingAttached(this.player)
        this.attachment = null
        val attribute = this.player.attributes.getInstance(Attributes.JUMP_STRENGTH) ?: return
        this.player.connection.send(ClientboundUpdateAttributesPacket(this.player.id, listOf(attribute)))
    }

    private fun tick() {
        if (this.hasRestrictedMovement) {
            if (this.player.distanceToSqr(this.position) > 0.5) {
                this.player.teleportTo(this.position)
            }
        }
    }

    public companion object {
        private const val SMALLEST_SCALE = 0.0625
        private val SMALLEST_SCALE_ATTRIBUTE = AttributeInstance(Attributes.SCALE) {}
        private val SMALLEST_JUMP_ATTRIBUTE = AttributeInstance(Attributes.JUMP_STRENGTH) {}

        @VisibleForTesting
        public var DEBUG: Boolean = false

        init {
            SMALLEST_SCALE_ATTRIBUTE.baseValue = SMALLEST_SCALE
            SMALLEST_JUMP_ATTRIBUTE.baseValue = 0.0
        }

        public fun ServerPlayer.restrictMovement(persist: Boolean = false) {
            this.getExtension<PlayerMovementRestrictionExtension>().restrictMovement(persist)
        }

        public fun ServerPlayer.unrestrictMovement() {
            this.getExtension<PlayerMovementRestrictionExtension>().unrestrictMovement()
        }

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> {
                it.addExtension(::PlayerMovementRestrictionExtension)
            }
            GlobalEventHandler.Server.register<PlayerTickEvent> {
                it.player.getExtension<PlayerMovementRestrictionExtension>().tick()
            }
            GlobalEventHandler.Server.register<PlayerDimensionChangeEvent> {
                it.player.unrestrictMovement()
            }
            GlobalEventHandler.Server.register<PlayerAttributeUpdatedEvent>(::onPlayerAttributeUpdated)
            GlobalEventHandler.Server.register<PlayerClientboundPacketEvent>(::onPlayerClientboundPacket)
        }

        private fun onPlayerAttributeUpdated(event: PlayerAttributeUpdatedEvent) {
            val (player, attribute) = event
            if (attribute.value() == Attributes.SCALE.value()) {
                val extension = player.getExtension<PlayerMovementRestrictionExtension>()
                if (extension.hasRestrictedMovement) {
                    val persist = extension.persist
                    extension.unrestrictMovement()
                    extension.restrictMovement(persist)
                }
            }
        }

        private fun onPlayerClientboundPacket(event: PlayerClientboundPacketEvent) {
            val (player, packet) = event
            if (packet is ClientboundUpdateAttributesPacket && player.id == packet.entityId) {
                val extension = player.getExtension<PlayerMovementRestrictionExtension>()
                if (extension.hasRestrictedMovement) {
                    val index = packet.values.indexOfFirst { snapshot ->
                        snapshot.attribute.value() == Attributes.JUMP_STRENGTH.value()
                    }
                    if (index >= 0) {
                        packet.values[index] = AttributeSnapshot(Attributes.JUMP_STRENGTH, 0.0, listOf())
                    }
                }
            }
        }
    }
}