/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.compat

import eu.pb4.polymer.core.api.entity.PolymerEntityUtils
import eu.pb4.polymer.core.impl.interfaces.PossiblyInitialPacket
import eu.pb4.polymer.core.impl.networking.PacketPatcher
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.world.entity.Entity

public object PolymerCompatLayer {
    public val loaded: Boolean = FabricLoader.getInstance().isModLoaded("polymer-core")

    public fun replacePacket(listener: ServerCommonPacketListenerImpl, packet: Packet<*>): Packet<*> {
        if (!this.loaded) {
            return packet
        }
        return PacketPatcher.replace(listener, packet)
    }

    public fun <T: Packet<ClientGamePacketListener>> updatePacket(old: Packet<*>, new: T): T {
        if (!this.loaded) {
            return new
        }

        if (PolymerEntityUtils.canHoldEntityContext(new)) {
            val entity = PolymerEntityUtils.getEntityContext(old)
            if (entity != null) {
                this.setEntityContext(new, entity)
            }
        }
        return new
    }

    public fun setEntityContext(packet: Packet<ClientGamePacketListener>, entity: Entity) {
        if (this.loaded) {
            PolymerEntityUtils.setEntityContext(packet, entity)
        }
    }

    public fun isInitial(packet: Packet<*>): Boolean {
        if (!this.loaded) {
            return false
        }

        return packet is PossiblyInitialPacket && packet.`polymer$getInitial`()
    }

    public fun setInitial(packet: Packet<*>) {
        if (this.loaded && packet is PossiblyInitialPacket) {
            packet.`polymer$setInitial`()
        }
    }
}