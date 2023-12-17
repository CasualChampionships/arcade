package net.casual.arcade.events.network

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerCommonPacketListenerImpl

public data class ClientboundPacketEvent(
    val server: MinecraftServer,
    val connection: ServerCommonPacketListenerImpl,
    val owner: GameProfile,
    val packet: Packet<*>
): CancellableEvent.Typed<Packet<*>>()