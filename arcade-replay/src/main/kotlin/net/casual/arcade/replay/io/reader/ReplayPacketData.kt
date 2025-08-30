/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.reader

import net.minecraft.network.ConnectionProtocol
import net.minecraft.network.protocol.Packet
import kotlin.time.Duration

public data class ReplayPacketData(
    val protocol: ConnectionProtocol,
    val packet: Packet<*>,
    val timestamp: Duration,
    private val release: () -> Unit = { }
): AutoCloseable {
    override fun close() {
        this.release.invoke()
    }
}