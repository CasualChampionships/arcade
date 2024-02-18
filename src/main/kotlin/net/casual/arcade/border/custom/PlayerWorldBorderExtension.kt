package net.casual.arcade.border.custom

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement
import net.casual.arcade.extensions.PlayerExtension
import net.minecraft.core.Direction.*
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.level.block.Blocks
import org.joml.Vector3f

public class PlayerWorldBorderExtension(owner: ServerGamePacketListenerImpl): PlayerExtension(owner)  {


    private val holder = BorderHolder()


    private val north = BlockDisplayElement()
    private val south = BlockDisplayElement()
    private val east = BlockDisplayElement()
    private val west = BlockDisplayElement()


    private var border_radius = 0F
    private var border_origin = Vector3f()

    private var border_lerp_ticks = 0

    init {



        //TODO: make this stained glass, change it based on: net.minecraft.world.level.border.BorderStatus




        north.startInterpolation()
        south.startInterpolation()
        east.startInterpolation()
        west.startInterpolation()



        holder.addElement(north)
        holder.addElement(south)
        holder.addElement(east)
        holder.addElement(west)



        EntityAttachment.ofTicking(holder, this.player)
        //TODO: Hide from other players.
        holder.startWatching(player)

    }


    private inner class BorderHolder: ElementHolder() {

        private fun relative_border_pos(vec: Vector3f): Vector3f {
            return vec.sub(player.position().toVector3f()).add(border_origin)
        }

        override fun onTick() {



            if (player.isSteppingCarefully) {
                border_radius = 100F
            }

            if (player.isFallFlying) {
                border_origin = player.position().toVector3f()
            }

            //TODO: Hook this into the level's custom world border.








            //TODO: start translations if there is a new "goal" (marked dirty)

            north.blockState = Blocks.BEDROCK.defaultBlockState()
            south.blockState = Blocks.BEDROCK.defaultBlockState()
            east.blockState = Blocks.BEDROCK.defaultBlockState()
            west.blockState = Blocks.BEDROCK.defaultBlockState()

            north.scale = Vector3f(2*border_radius, 200F, 0F)
            south.scale = Vector3f(2*border_radius, 200F, 0F)
            east.scale = Vector3f(0F, 200F, 2*border_radius)
            west.scale = Vector3f(0F, 200F, 2*border_radius)


            north.translation = relative_border_pos(NORTH.step().mul(border_radius).add(NORTH.counterClockWise.step().mul(border_radius)))
            south.translation = relative_border_pos(SOUTH.step().mul(border_radius).add(SOUTH.clockWise.step().mul(border_radius)))
            east.translation = relative_border_pos(EAST.step().mul(border_radius).add(EAST.counterClockWise.step().mul(border_radius)))
            west.translation = relative_border_pos(WEST.step().mul(border_radius).add(WEST.clockWise.step().mul(border_radius)))

//            border_ticks = max(0F, border_ticks-0.5F)
            border_radius = 0F
        }



    }



}