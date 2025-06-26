/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.utils

import eu.pb4.polymer.virtualentity.api.ElementHolder
import net.casual.arcade.utils.asClientGamePacket
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket

public class BundlingElementHolder: ElementHolder() {
    private val bundle = ArrayList<Packet<in ClientGamePacketListener>>()

    override fun tick() {
        super.tick()
        val packet = ClientboundBundlePacket(this.bundle)
        for (connection in this.watchingPlayers) {
            connection.send(packet)
        }
        this.bundle.clear()
    }

    override fun sendPacket(packet: Packet<out ClientGamePacketListener>) {
        this.bundle.add(packet.asClientGamePacket())
    }
}