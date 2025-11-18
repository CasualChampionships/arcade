/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.compat

import eu.pb4.polymer.core.impl.networking.PacketPatcher
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.Packet
import net.minecraft.server.network.ServerCommonPacketListenerImpl

public object PolymerCompatLayer {
    public val loaded: Boolean = FabricLoader.getInstance().isModLoaded("polymer-core")

    public fun replace(listener: ServerCommonPacketListenerImpl, packet: Packet<*>): Packet<*> {
        if (!this.loaded) {
            return packet
        }
        return PacketPatcher.replace(listener, packet)
    }
}