package net.casual.arcade.commands

import net.casual.arcade.Arcade
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandEvent

internal object ArcadeCommands {
    fun registerCommands() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> {
            MinigameCommand.register(it.dispatcher)

            if (Arcade.DEBUG) {
                DebugCommand.register(it.dispatcher)
            }
        }
    }
}