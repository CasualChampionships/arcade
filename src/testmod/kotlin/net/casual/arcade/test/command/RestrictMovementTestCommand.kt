package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.restrictMovement
import net.casual.arcade.minigame.extensions.PlayerMovementRestrictionExtension.Companion.unrestrictMovement
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

object RestrictMovementTestCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("restrict-movement-test") {
            literal("restrict") {
                executes(::restrictMovement)
            }
            literal("unrestrict") {
                executes(::unrestrictMovement)
            }
        }
    }

    private fun restrictMovement(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        player.restrictMovement()
    }

    private fun unrestrictMovement(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        player.unrestrictMovement()
    }
}