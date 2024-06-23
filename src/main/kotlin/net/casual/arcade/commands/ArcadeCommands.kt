package net.casual.arcade.commands

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent

internal object ArcadeCommands {
    fun registerCommands() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> {
            it.register(MinigameCommand, TeamCommandModifier, WorldBorderCommandModifier)
        }
    }
}