package net.casualuhc.arcade.events.server

import net.casualuhc.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

/**
 * This event is fired just before the [MinecraftServer]
 * started its first tick. Everything by this point should
 * be loaded, for example worlds.
 *
 * @param server The [MinecraftServer] instance that is loaded.
 */
data class ServerLoadedEvent(
    val server: MinecraftServer
): Event()