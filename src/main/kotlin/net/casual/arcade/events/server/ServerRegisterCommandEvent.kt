package net.casual.arcade.events.server

import com.mojang.brigadier.CommandDispatcher
import net.casual.arcade.commands.Command
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

public data class ServerRegisterCommandEvent(
    val dispatcher: CommandDispatcher<CommandSourceStack>,
    val context: CommandBuildContext
): SafeServerlessEvent {
    public fun register(vararg commands: Command) {
        for (command in commands) {
            command.register(this.dispatcher, this.context)
        }
    }
}