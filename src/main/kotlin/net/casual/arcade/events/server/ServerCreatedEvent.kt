package net.casual.arcade.events.server

import net.casual.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

/**
 * Event fired when an instance of a [MinecraftServer]
 * is instantiated.
 *
 * It is extremely important to note that this may be
 * called before the server has properly initialised,
 * e.g. before worlds are loaded, before configs are loaded.
 *
 * @param server The [MinecraftServer] that was instantiated.
 */
data class ServerCreatedEvent(
    /**
     * The [MinecraftServer] that was instantiated.
     */
    val server: MinecraftServer
): Event