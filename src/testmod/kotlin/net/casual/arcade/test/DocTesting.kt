package net.casual.arcade.test

import com.mojang.brigadier.Command
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.arguments.RegistryElementArgument
import net.casual.arcade.commands.hidden.HiddenCommandManager
import net.casual.arcade.commands.manager.CommandManager
import net.casual.arcade.commands.manager.GlobalCommandManager
import net.casual.arcade.utils.TimeUtils.Seconds
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.network.chat.ClickEvent
import net.minecraft.server.MinecraftServer
import net.minecraft.world.entity.animal.cow.CowVariant

object DocTesting {
    fun xyz() {
        val server: MinecraftServer = null!!
        val manager = CommandManager(server)
        manager.register(object: CommandTree {
            override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
                TODO("Not yet implemented")
            }
        })
        GlobalCommandManager.addManager(manager)

        GlobalCommandManager.removeManager(manager)
    }

    fun createExampleCommand(): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("example") {
            argument("element", RegistryElementArgument.element(Registries.COW_VARIANT)) {
                executes {
                    val holder: Holder.Reference<CowVariant> = RegistryElementArgument.getHolder(it, "element")
                    val variant: CowVariant = RegistryElementArgument.getElement(it, "element")
                    Command.SINGLE_SUCCESS
                }
            }
        }
    }
}