package net.casual.arcade.events.server

import net.casual.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

/**
 * This event is fired just before the [MinecraftServer]
 * started its first tick. Everything by this point should
 * be loaded, for example, worlds.
 *
 * @param server The [MinecraftServer] instance that is loaded.
 */
public data class ServerLoadedEvent(
    /**
     * The [MinecraftServer] instance that is loaded.
     */
    val server: MinecraftServer
): Event