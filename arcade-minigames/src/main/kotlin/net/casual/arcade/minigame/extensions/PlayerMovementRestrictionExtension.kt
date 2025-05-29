/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.extensions

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.VirtualEntityUtils
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.SimpleEntityElement
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerAttributeUpdatedEvent
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerTickEvent
import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.EntityExtensionEvent.Companion.getExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.locationWithLevel
import net.casual.arcade.utils.teleportTo
import net.minecraft.core.Direction
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.Tag
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket
import net.minecraft.network.protocol.game.ClientboundUpdateAttributesPacket.AttributeSnapshot
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.phys.Vec3
import org.jetbrains.annotations.VisibleForTesting
import kotlin.jvm.optionals.getOrNull

public class PlayerMovementRestrictionExtension(player: ServerPlayer): PlayerExtension(player), DataExtension {
    private var attachment: EntityAttachment? = null
    private var position: Vec3 = Vec3.ZERO
    private var persist: Boolean = false

    public val hasRestrictedMovement: Boolean
        get() = this.attachment != null

    override fun getName(): String {
        return "${ArcadeUtils.MOD_ID}_movement_restriction_extension"
    }

    override fun deserialize(element: Tag) {
        val data = Data.CODEC.parse(NbtOps.INSTANCE, element).result().getOrNull() ?: return
        this.persist = data.persist
        this.position = data.position
        if (this.persist) {
            GlobalTickedScheduler.later {
                this.restrictMovement(true)
            }
        }
    }

    override fun serialize(): Tag? {
        if (!this.persist || !this.hasRestrictedMovement) {
            return null
        }
        val data = Data(this.position, true)
        return Data.CODEC.encodeStart(NbtOps.INSTANCE, data).result().getOrNull()
    }

    private fun restrictMovement(persist: Boolean) {
        this.persist = persist
        this.position = this.player.position()
        if (this.attachment != null) {
            return
        }

        val dimensions = this.player.getDimensions(this.player.pose)
        val holder = ElementHolder()
        val packets = ArrayList<Packet<*>>(12)
        for (direction in Direction.entries) {
            val vehicle = SimpleEntityElement(EntityType.TEXT_DISPLAY)
            val element = SimpleEntityElement(EntityType.SHULKER)
            vehicle.isInvisible = true
            if (!debug) {
                element.isInvisible = true
            }

            val horizontalSize = dimensions.width
            val verticalSize = dimensions.height
            val unit = direction.unitVec3

            val offsetX = unit.x * (horizontalSize / 2 + SMALLEST_SCALE / 2)
            val offsetY = ((unit.y + 1) / 2) * unit.y * verticalSize + SMALLEST_SCALE * unit.y -
                    (unit.y - 1) * (unit.y + 1) * verticalSize / 2
            val offsetZ = unit.z * (horizontalSize / 2 + SMALLEST_SCALE / 2)

            vehicle.offset = Vec3(offsetX, offsetY, offsetZ)
            holder.addElement(vehicle)
            holder.addElement(element)
            packets.add(ClientboundUpdateAttributesPacket(element.entityId, listOf(SMALLEST_SCALE_ATTRIBUTE)))
            packets.add(VirtualEntityUtils.createRidePacket(vehicle.entityId, intArrayOf(element.entityId)))
        }
        val attachment = EntityAttachment(holder, player, false)
        attachment.startWatching(this.player)
        for (packet in packets) {
            this.player.connection.send(packet)
        }
        this.player.connection.send(ClientboundUpdateAttributesPacket(this.player.id, listOf(SMALLEST_JUMP_ATTRIBUTE)))
        this.player.teleportTo(this.player.locationWithLevel)
        this.attachment = attachment
    }

    private fun unrestrictMovement() {
        val attachment = this.attachment ?: return
        attachment.destroy()
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

    private data class Data(val position: Vec3, val persist: Boolean) {
        companion object {
            val CODEC: Codec<Data> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Vec3.CODEC.fieldOf("position").forGetter(Data::position),
                    Codec.BOOL.fieldOf("persist").forGetter(Data::persist)
                ).apply(instance, ::Data)
            }
        }
    }

    public companion object {
        private const val SMALLEST_SCALE = 0.0625
        private val SMALLEST_SCALE_ATTRIBUTE = AttributeInstance(Attributes.SCALE) {}
        private val SMALLEST_JUMP_ATTRIBUTE = AttributeInstance(Attributes.JUMP_STRENGTH) {}

        @VisibleForTesting
        public var debug: Boolean = false

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