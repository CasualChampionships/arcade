package net.casual.arcade.commands

import com.mojang.brigadier.arguments.StringArgumentType
import net.casual.arcade.Arcade
import net.casual.arcade.commands.arguments.*
import net.casual.arcade.commands.type.CustomStringArgumentInfo
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerRegisterCommandArgumentEvent
import net.casual.arcade.events.server.ServerRegisterCommandEvent

internal object ArcadeCommands {
    fun registerCommands() {
        GlobalEventHandler.register<ServerRegisterCommandEvent> {
            MinigameCommand.register(it.dispatcher)

            if (Arcade.debug) {
                DebugCommand.register(it.dispatcher)
            }
        }

        GlobalEventHandler.register<ServerRegisterCommandArgumentEvent> {
            it.addArgument(EnumArgument::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.SINGLE_WORD))
            it.addArgument(MappedArgument::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.SINGLE_WORD))
            it.addArgument(TimeArgument::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.QUOTABLE_PHRASE))
            it.addArgument(TimeZoneArgument::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.QUOTABLE_PHRASE))

            it.addArgument(MinigameArgument::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.QUOTABLE_PHRASE))
            it.addArgument(MinigameArgument.SettingsName::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.SINGLE_WORD))
            it.addArgument(MinigameArgument.SettingsOption::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.SINGLE_WORD))
            it.addArgument(MinigameArgument.SettingsValue::class.java, CustomStringArgumentInfo(StringArgumentType.StringType.QUOTABLE_PHRASE))
        }
    }
}