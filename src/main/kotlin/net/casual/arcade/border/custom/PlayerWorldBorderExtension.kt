package net.casual.arcade.border.custom

import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.EntityAttachment
import eu.pb4.polymer.virtualentity.api.elements.AbstractElement
import eu.pb4.polymer.virtualentity.api.elements.BlockDisplayElement
import it.unimi.dsi.fastutil.ints.IntList
import net.casual.arcade.Arcade
import net.casual.arcade.extensions.PlayerExtension
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.phys.Vec3
import org.joml.Vector3f
import java.util.function.Consumer

public class PlayerWorldBorderExtension(owner: ServerGamePacketListenerImpl): PlayerExtension(owner)  {

    private val border_element = BorderDisplay()
    private val holder = Holder(border_element)




    init {
        EntityAttachment.ofTicking(holder, this.player)
    }


    private inner class BorderDisplay : AbstractElement(){
        private val north = BlockDisplayElement()
        private val south = BlockDisplayElement()
        private val east = BlockDisplayElement()
        private val west = BlockDisplayElement()




        init {

            //TODO: make this stained glass, change it based on: net.minecraft.world.level.border.BorderStatus

            north.blockState = Blocks.BEDROCK.defaultBlockState()
            south.blockState = Blocks.BEDROCK.defaultBlockState()
            east.blockState = Blocks.BEDROCK.defaultBlockState()
            west.blockState = Blocks.BEDROCK.defaultBlockState()

            north.scale = Vector3f(0F, 1F, 1F)




        }

        override fun getEntityIds(): IntList {
            return IntList.of(north.entityId, south.entityId, east.entityId, west.entityId)
        }

        private fun createSpawnPacket(display: BlockDisplayElement): ClientboundAddEntityPacket {
            return ClientboundAddEntityPacket(
                display.entityId,
                display.uuid,
                player.x,
                player.y,
                player.z,
                display.pitch,
                display.yaw,
                EntityType.BLOCK_DISPLAY,
                0,
                Vec3.ZERO,
                display.yaw.toDouble()
            )
        }

        override fun startWatching(player: ServerPlayer?, consumer: Consumer<Packet<ClientGamePacketListener>>) {
            consumer.accept(createSpawnPacket(north))
            consumer.accept(createSpawnPacket(south))
            consumer.accept(createSpawnPacket(east))
            consumer.accept(createSpawnPacket(west))
        }

        private fun sendTrackerUpdates(display: BlockDisplayElement) {
            if (display.dataTracker.isDirty) {
                val dirty = display.dataTracker.dirtyEntries
                val holder = this.holder
                if (dirty != null && holder != null) {
                    for (connection in holder.watchingPlayers) {
                        connection.send(ClientboundSetEntityDataPacket(display.entityId, dirty))
                    }
                }
            }
        }

        override fun stopWatching(player: ServerPlayer?, packetConsumer: Consumer<Packet<ClientGamePacketListener>>?) {
            //Should we "kill" the entity here?


            Arcade.logger.info("Stop Watching for custom border")
        }

        override fun notifyMove(oldPos: Vec3?, currentPos: Vec3?, delta: Vec3?) {
            // Maybe use this to update the client after it teleports?
        }

        override fun tick() {
            sendTrackerUpdates(this.north)
            sendTrackerUpdates(this.south)
            sendTrackerUpdates(this.east)
            sendTrackerUpdates(this.west)

        }


    }


    private inner class Holder(
        val element: PlayerWorldBorderExtension.BorderDisplay
    ): ElementHolder() {
        init {
            this.addElement(this.element)
        }

        override fun onTick() {
            this.attachment?.updateCurrentlyTracking(ArrayList(this.watchingPlayers))
        }

        override fun startWatching(connection: ServerGamePacketListenerImpl): Boolean {
                if (super.startWatching(connection)) {
                    //TODO: Something might be missing here
                    return true
                } else {
                    this.stopWatching(connection)
                }


            return false
        }

        override fun stopWatching(player: ServerGamePacketListenerImpl): Boolean {
            return super.stopWatching(player)
        }
    }
}