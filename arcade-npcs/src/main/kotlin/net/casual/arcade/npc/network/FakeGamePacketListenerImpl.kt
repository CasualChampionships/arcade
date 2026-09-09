/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.network

import io.netty.channel.ChannelFutureListener
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientboundPlayerPositionPacket
import net.minecraft.network.protocol.game.ServerboundAcceptTeleportationPacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.TickTask
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl

public open class FakeGamePacketListenerImpl(
    server: MinecraftServer,
    connection: Connection,
    player: ServerPlayer,
    cookie: CommonListenerCookie
): ServerGamePacketListenerImpl(server, connection, player, cookie) {
    override fun tick() {
        // We do this here to keep the player tick
        // phase consistent with vanilla players
        this.player.doTick()
    }

    protected open fun receivesPackets(): Boolean {
        return false
    }

    override fun send(packet: Packet<*>, listener: ChannelFutureListener?) {
        if (packet is ClientboundPlayerPositionPacket) {
            val position = this.player.position()
            val rotation = this.player.rotationVector
            this.server.schedule(TickTask(this.server.tickCount) {
                this.handleAcceptTeleportPacket(
                    ServerboundAcceptTeleportationPacket(packet.id, position.x, position.y, position.z, rotation.y, rotation.x)
                )
            })
        }
        if (this.receivesPackets()) {
            super.send(packet, listener)
        }
    }
}