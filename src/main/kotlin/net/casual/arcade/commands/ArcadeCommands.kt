package net.casual.arcade.commands

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent

internal object ArcadeCommands {
    fun registerCommands() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> {
            MinigameCommand.register(it.dispatcher)
            TeamCommandModifier.register(it.dispatcher)
            WorldBorderCommandModifier.register(it.dispatcher)

            ArcadeCommand.register(it.dispatcher)
        }
    }
}