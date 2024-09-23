package net.casual.arcade.events.server

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.core.Event
import net.minecraft.server.MinecraftServer

/**
 * Event that is fired every tick on the server.
 *
 * This has two possible phases when it can be triggered.
 * [BuiltInEventPhases.PRE] and [BuiltInEventPhases.POST].
 *
 * By default, this event is fired in the [BuiltInEventPhases.PRE] phase.
 */
public data class ServerTickEvent(
    /**
     * The [MinecraftServer] instance that was ticked.
     */
    val server: MinecraftServer
): Event