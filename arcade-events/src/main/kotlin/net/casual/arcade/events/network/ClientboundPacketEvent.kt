package net.casual.arcade.events.network

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.server.ServerOffThreadEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer

public data class ClientboundPacketEvent(
    val server: MinecraftServer,
    val owner: GameProfile,
    var packet: Packet<*>
): CancellableEvent.Default(), ServerOffThreadEvent