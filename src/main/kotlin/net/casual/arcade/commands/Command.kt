package net.casual.arcade.commands

import com.mojang.brigadier.CommandDispatcher
import net.minecraft.commands.CommandSourceStack

public interface Command {
    public fun register(dispatcher: CommandDispatcher<CommandSourceStack>)
}