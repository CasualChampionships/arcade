package net.casual.arcade.events.server

import com.mojang.brigadier.CommandDispatcher
import net.casual.arcade.events.core.Event
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

data class ServerRegisterCommandEvent(
    val dispatcher: CommandDispatcher<CommandSourceStack>,
    val context: CommandBuildContext
): Event