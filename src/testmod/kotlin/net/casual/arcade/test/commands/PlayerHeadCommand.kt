package net.casual.arcade.test.commands

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.pack.font.heads.PixelGridHeadComponents
import net.casual.arcade.utils.player.StaticResolvableProfile
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

@Suppress("unused")
object PlayerHeadCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("player-head") {
            literal("display") {
                literal("username") {
                    argument("username", StringArgumentType.word()) {
                        argument("shift", IntegerArgumentType.integer(-256, 256)) {
                            executes(::displayPlayerHead)
                        }
                        executes { displayPlayerHead(it, 0) }
                    }
                }
                literal("default") {
                    argument("shift", IntegerArgumentType.integer(-256, 256)) {
                        executes(::displayDefaultHead)
                    }
                    executes { displayDefaultHead(it, 0) }
                }
            }
        }
    }

    private fun displayPlayerHead(
        context: CommandContext<CommandSourceStack>,
        shift: Int = IntegerArgumentType.getInteger(context, "shift")
    ): Int {
        val username = StringArgumentType.getString(context, "username")
        val component = PixelGridHeadComponents.getHeadOrDefaultFor(StaticResolvableProfile(username), context.source.server, shift)
        return context.source.success(Component.literal("${username}'s head: ").append(component))
    }

    private fun displayDefaultHead(
        context: CommandContext<CommandSourceStack>,
        shift: Int = IntegerArgumentType.getInteger(context, "shift")
    ): Int {
        val component = PixelGridHeadComponents.get(shift, context.source.server.services()).getDefault()
        return context.source.success(Component.literal("Default head: ").append(component))
    }
}