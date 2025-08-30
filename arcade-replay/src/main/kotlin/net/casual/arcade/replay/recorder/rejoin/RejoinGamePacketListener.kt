/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.rejoin

import io.netty.channel.ChannelFutureListener
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.minecraft.network.Connection
import net.minecraft.network.protocol.Packet
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl

public class RejoinGamePacketListener(
    replay: RejoinedReplayPlayer,
    connection: Connection,
    cookies: CommonListenerCookie
): ServerGamePacketListenerImpl(replay.levelServer, connection, replay, cookies) {
    // We don't store extra fields in this class because certain
    // mods like sending packets DURING the construction, *cough* syncmatica *cough*
    private val replay: RejoinedReplayPlayer
        get() = this.player as RejoinedReplayPlayer

    override fun send(packet: Packet<*>, sendListener: ChannelFutureListener?) {
        try {
            this.replay.recorder.record(packet)
        } catch (e: Exception) {
            val name = this.player.scoreboardName
            ArcadeUtils.logger.error("Failed to record rejoin packet {} for {}", packet, name, e)
        }
    }
}