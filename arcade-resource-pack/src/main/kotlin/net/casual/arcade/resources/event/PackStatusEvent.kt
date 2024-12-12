package net.casual.arcade.resources.event

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.Event
import net.casual.arcade.resources.pack.PackStatus
import net.minecraft.server.MinecraftServer
import java.util.*

public data class PackStatusEvent(
    val server: MinecraftServer,
    val owner: GameProfile,
    val uuid: UUID,
    val status: PackStatus
): Event