/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.extensions

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.player.PlayerClientboundPacketEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.getExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundInitializeBorderPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderCenterPacket
import net.minecraft.network.protocol.game.ClientboundSetBorderLerpSizePacket
import net.minecraft.network.protocol.game.ClientboundSetBorderSizePacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder
import org.joml.Vector3f

public class PlayerWorldBorderExtension(player: ServerPlayer): PlayerExtension(player)  {
    private val holder = ElementHolder()

    private val north = BlockDisplayElement()
    private val south = BlockDisplayElement()
    private val east = BlockDisplayElement()
    private val west = BlockDisplayElement()

    private var display: Boolean = false

    // TODO: Currently using the vanilla WB
    //  - This actually may not be an issue?
    //    As long as we are blocking the world border packets, we can use the
    //    vanilla border for the actual calculations and such, just change rendering.

    //TODO: Either don't block border warning packets, or handle them ourselves.
    private val border: WorldBorder
        get() = this.player.level().worldBorder

    init {
        this.holder.addElement(this.north)
        this.holder.addElement(this.south)
        this.holder.addElement(this.east)
        this.holder.addElement(this.west)

        this.updateState()
        this.updateScale()
        this.updateTranslation()

        // FIXME: wtf is this jank
        GlobalTickedScheduler.later {
            BorderAttachment()
        }
    }

    private fun setDisplay(display: Boolean) {
        this.display = display
        // TODO: Send updated state to client
    }

    private fun updateState() {
        val state = when (this.border.status) {
            BorderStatus.SHRINKING -> Blocks.RED_STAINED_GLASS
            BorderStatus.GROWING -> Blocks.LIME_STAINED_GLASS
            else -> Blocks.LIGHT_BLUE_STAINED_GLASS
        }.defaultBlockState()

        this.north.blockState = state
        this.south.blockState = state
        this.east.blockState = state
        this.west.blockState = state
    }

    private fun updateScale() {
        val diameter = this.border.size.toFloat()

        // TODO: We probably only want to scale to what the
        //   player can actually see...
        //   We don't want to send the north border if it's thousands of blocks away...
        this.north.scale = Vector3f(diameter, 300F, 0F)
        this.south.scale = Vector3f(diameter, 300F, 0F)
        this.east.scale = Vector3f(0F, 300F, diameter)
        this.west.scale = Vector3f(0F, 300F, diameter)
    }

    private fun updateTranslation() {
        val radius = this.border.size.toFloat() / 2.0F
        val north = Direction.NORTH.step().mul(radius)
        val west = Direction.WEST.step().mul(radius)
        val playerPos = this.player.position().toVector3f()
        val borderPos = Vector3f(this.border.centerX.toFloat(), 0F, this.border.centerZ.toFloat())

        this.north.translation = north.add(west, Vector3f()).sub(playerPos).add(borderPos)
        this.south.translation = north.negate(Vector3f()).add(west).sub(playerPos).add(borderPos)
        this.east.translation = west.negate(Vector3f()).add(north).sub(playerPos).add(borderPos)
        this.west.translation = west.add(north).sub(playerPos).add(borderPos)
    }

    private fun setupInterpolation() {
        val lerpTime = this.border.lerpRemainingTime.toInt()
        this.north.interpolationDuration = lerpTime
        this.south.interpolationDuration = lerpTime
        this.east.interpolationDuration = lerpTime
        this.west.interpolationDuration = lerpTime

        this.north.startInterpolation()
        this.south.startInterpolation()
        this.east.startInterpolation()
        this.west.startInterpolation()
    }

    private fun isDirty(): Boolean {
        return this.north.scale.x() != this.border.size.toFloat()
    }

    private inner class BorderAttachment: EntityAttachment(this.holder, this.player, true) {
        init {
            super.startWatching(player)
        }

        override fun startWatching(handler: ServerPlayer) {
            // Do nothing...
        }

        override fun startWatching(handler: ServerGamePacketListenerImpl) {
            // Do nothing...
        }

        override fun updateCurrentlyTracking(currentlyTracking: MutableCollection<ServerGamePacketListenerImpl>?) {
            // Do nothing...
        }

        override fun tick() {
            // TODO: Interpolate
            updateState()
            updateScale()
            updateTranslation()

            holder.tick()
        }
    }

    public companion object {
        private val BLOCKED_PACKETS = setOf(
            ClientboundInitializeBorderPacket::class.java,
            ClientboundSetBorderSizePacket::class.java,
            ClientboundSetBorderCenterPacket::class.java,
            ClientboundSetBorderLerpSizePacket::class.java
        )

        public fun ServerPlayer.setUseCustomDisplayWorldBorder(display: Boolean) {
            this.getExtension<PlayerWorldBorderExtension>().setDisplay(display)
        }

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> { event ->
                event.addExtension(::PlayerWorldBorderExtension)
            }

            GlobalEventHandler.Server.register<PlayerClientboundPacketEvent> { event ->
                if (this.shouldBlockPacket(event.player, event.packet)) {
                    event.cancel()
                }
            }
        }

        private fun shouldBlockPacket(player: ServerPlayer, packet: Packet<*>): Boolean {
            val extension = player.getExtension<PlayerWorldBorderExtension>()
            return extension.display && BLOCKED_PACKETS.contains(packet::class.java)
        }
    }
}

