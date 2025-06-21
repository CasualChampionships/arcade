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
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.addExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.minecraft.core.Direction.NORTH
import net.minecraft.core.Direction.WEST
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

internal class PlayerWorldBorderExtension(owner: ServerGamePacketListenerImpl): PlayerExtension(owner)  {
    private val holder = ElementHolder()

    private val north = BlockDisplayElement()
    private val south = BlockDisplayElement()
    private val east = BlockDisplayElement()
    private val west = BlockDisplayElement()

    // TODO: Currently using the vanilla WB
    //  - This actually may not be an issue?
    //    As long as we are blocking the world border packets, we can use the
    //    vanilla border for the actual calculations and such, just change rendering.

    //TODO: Either don't block border warning packets, or handle them ourselves.
    private val border: WorldBorder
        get() = this.player.level().worldBorder

    init {
        holder.addElement(north)
        holder.addElement(south)
        holder.addElement(east)
        holder.addElement(west)

        this.updateState()
        this.updateScale()
        this.updateTranslation()

        GlobalTickedScheduler.later {
            BorderAttachment()
        }
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
        val north = NORTH.step().mul(radius)
        val west = WEST.step().mul(radius)
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
    companion object {
        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerJoinEvent> { (player) ->
                player.addExtension(PlayerWorldBorderExtension(player.connection))
            }

            GlobalEventHandler.Server.register<PlayerClientboundPacketEvent> {
                if (
                    it.packet is ClientboundInitializeBorderPacket ||
                    it.packet is ClientboundSetBorderSizePacket ||
                    it.packet is ClientboundSetBorderCenterPacket ||
                    it.packet is ClientboundSetBorderLerpSizePacket
                ) {
                    it.cancel()
                }
            }
        }


    }
}

