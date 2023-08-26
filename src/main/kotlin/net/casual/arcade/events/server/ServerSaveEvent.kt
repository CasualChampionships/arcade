package net.casual.arcade.events.server

import net.casual.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

data class ServerSaveEvent(
    val server: MinecraftServer
): Event