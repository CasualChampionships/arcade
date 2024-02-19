package net.casual.arcade.border.custom

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.minecraft.core.Direction.*
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.level.border.WorldBorder
import org.joml.Vector3f

public class PlayerWorldBorderExtension(owner: ServerGamePacketListenerImpl): PlayerExtension(owner)  {
    private val holder = ElementHolder()

    private val north = BlockDisplayElement()
    private val south = BlockDisplayElement()
    private val east = BlockDisplayElement()
    private val west = BlockDisplayElement()

    // TODO: Currently using the vanilla WB
    //  - This actually may not be an issue?
    //    As long as we are blocking the world border packets, we can use the
    //    vanilla border for the actual calculations and such, just change rendering.
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

        this.north.scale = Vector3f(diameter, 200F, 0F)
        this.south.scale = Vector3f(diameter, 200F, 0F)
        this.east.scale = Vector3f(0F, 200F, diameter)
        this.west.scale = Vector3f(0F, 200F, diameter)
    }

    private fun updateTranslation() {
        val radius = this.border.size.toFloat() / 2.0F
        val north = NORTH.step().mul(radius)
        val west = WEST.step().mul(radius)
        val position = this.player.position().toVector3f()

        this.north.translation = north.add(west, Vector3f()).sub(position)
        this.south.translation = north.negate(Vector3f()).add(west).sub(position)
        this.east.translation = west.negate(Vector3f()).add(north).sub(position)
        this.west.translation = west.add(north).sub(position)
    }

    private inner class BorderAttachment: EntityAttachment(this.holder, this.player, false) {
        init {
            this.startWatching(player)
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

        override fun shouldTick(): Boolean {
            return true
        }
    }
}