package net.casual.arcade.events.server

import com.mojang.brigadier.CommandDispatcher
import net.casual.arcade.events.common.MissingExecutorEvent
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

public data class ServerRegisterCommandEvent(
    val dispatcher: CommandDispatcher<CommandSourceStack>,
    val context: CommandBuildContext
): MissingExecutorEvent