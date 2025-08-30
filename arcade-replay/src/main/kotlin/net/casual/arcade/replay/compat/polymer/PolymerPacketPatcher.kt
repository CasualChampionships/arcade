/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.compat.polymer

import eu.pb4.polymer.core.impl.networking.PacketPatcher
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.Packet
import net.minecraft.server.network.ServerCommonPacketListenerImpl

internal object PolymerPacketPatcher {
    private val hasPolymer = FabricLoader.getInstance().isModLoaded("polymer-core")

    internal fun replace(listener: ServerCommonPacketListenerImpl, packet: Packet<*>): Packet<*> {
        if (this.hasPolymer) {
            return PacketPatcher.replace(listener, packet)
        }
        return packet
    }
}