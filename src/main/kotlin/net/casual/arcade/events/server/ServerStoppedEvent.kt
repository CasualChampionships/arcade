package net.casual.arcade.events.server

import net.casual.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

data class ServerStoppedEvent(
    val server: MinecraftServer
): Event