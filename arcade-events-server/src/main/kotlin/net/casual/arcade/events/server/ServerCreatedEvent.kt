package net.casual.arcade.events.server

import net.casual.arcade.events.common.MissingExecutorEvent
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
public data class ServerCreatedEvent(
    /**
     * The [MinecraftServer] that was instantiated.
     */
    val server: MinecraftServer
): MissingExecutorEvent