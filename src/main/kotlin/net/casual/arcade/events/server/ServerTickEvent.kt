package net.casual.arcade.events.server

import net.casual.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

public data class ServerTickEvent(
    val server: MinecraftServer
): Event