package net.casualuhc.arcade.events.server

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

class ServerStoppedEvent(
    val server: MinecraftServer
): Event()