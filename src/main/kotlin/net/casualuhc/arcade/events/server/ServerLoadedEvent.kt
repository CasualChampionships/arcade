package net.casualuhc.arcade.events.server

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

class ServerLoadedEvent(
    val server: MinecraftServer
): Event()