package net.casual.arcade.events.player

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import java.net.SocketAddress

public data class PlayerCanLoginEvent(
    val server: MinecraftServer,
    val profile: GameProfile,
    val address: SocketAddress
): CancellableEvent.Typed<Component>()