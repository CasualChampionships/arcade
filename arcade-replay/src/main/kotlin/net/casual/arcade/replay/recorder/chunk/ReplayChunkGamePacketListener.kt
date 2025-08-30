/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.chunk

import io.netty.channel.ChannelFutureListener
import net.casual.arcade.replay.recorder.rejoin.RejoinConnection
import net.minecraft.network.protocol.Packet
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ServerGamePacketListenerImpl

public class ReplayChunkGamePacketListener(
    private val recorder: ReplayChunkRecorder,
    player: ServerPlayer
): ServerGamePacketListenerImpl(
    recorder.server,
    RejoinConnection(),
    player,
    CommonListenerCookie.createInitial(recorder.profile, false)
) {
    override fun send(packet: Packet<*>, sendListener: ChannelFutureListener?) {
        this.recorder.record(packet)
    }
}