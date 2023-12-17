package net.casual.arcade.events.network

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.core.Event
import net.casual.arcade.resources.PackStatus
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import java.util.UUID

public data class PackStatusEvent(
    val server: MinecraftServer,
    val owner: GameProfile,
    val uuid: UUID,
    val status: PackStatus
): Event