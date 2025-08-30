/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server

import net.casual.arcade.events.BuiltInEventPhases
import net.casual.arcade.events.common.Event
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents
import net.minecraft.server.MinecraftServer

/**
 * This event is fired before the [MinecraftServer]
 * started its first tick.
 * See [PHASE_PRE] and [PHASE_POST] for the event phases.
 *
 * @param server The [MinecraftServer] instance that is loaded.
 */
public data class ServerStartEvent(
    /**
     * The [MinecraftServer] instance that is loaded.
     */
    val server: MinecraftServer
): Event {
    public companion object {
        /**
         * Synonymous with [ServerLifecycleEvents.SERVER_STARTING].
         *
         * This gets fired before the server is initialized, and worlds are loaded.
         *
         * This is *not* the default phase for this event.
         */
        public const val PHASE_PRE: String = BuiltInEventPhases.PRE

        /**
         * Synonymous with [ServerLifecycleEvents.SERVER_STARTED]
         *
         * Everything by this point should be loaded, for example, worlds.
         *
         * This is the default phase for this event.
         */
        public const val PHASE_POST: String = BuiltInEventPhases.POST
    }
}