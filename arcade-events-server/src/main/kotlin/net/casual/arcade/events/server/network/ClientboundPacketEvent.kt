package net.casual.arcade.events.server.network

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.common.MissingExecutorEvent
import net.minecraft.network.protocol.Packet
import net.minecraft.server.MinecraftServer

public data class ClientboundPacketEvent(
    val server: MinecraftServer,
    val owner: GameProfile,
    var packet: Packet<*>
): CancellableEvent.Default(), MissingExecutorEvent