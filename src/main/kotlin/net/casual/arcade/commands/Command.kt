package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

public interface Command {
    public fun register(dispatcher: CommandDispatcher<CommandSourceStack>) {

    }

    public fun register(dispatcher: CommandDispatcher<CommandSourceStack>, buildContext: CommandBuildContext) {
        this.register(dispatcher)
    }
}