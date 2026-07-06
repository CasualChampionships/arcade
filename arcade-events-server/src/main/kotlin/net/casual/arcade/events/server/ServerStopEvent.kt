/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server

import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.minecraft.server.MinecraftServer

public data class ServerStopEvent(
    val server: MinecraftServer
): ServerSideEvent {
    public companion object {
        public const val PHASE_PRE: Int = BuiltInEventPhases.PRE

        public const val PHASE_POST: Int = BuiltInEventPhases.POST
    }
}