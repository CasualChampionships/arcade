package net.casualuhc.arcade.events.server

import com.mojang.brigadier.CommandDispatcher
import net.casualuhc.arcade.events.core.Event
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

class ServerRegisterCommandEvent(
    val dispatcher: CommandDispatcher<CommandSourceStack>,
    val context: CommandBuildContext
): Event()