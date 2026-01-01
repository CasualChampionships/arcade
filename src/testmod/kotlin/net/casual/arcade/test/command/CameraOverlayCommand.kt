package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.visuals.utils.clearCameraOverlay
import net.casual.arcade.visuals.utils.setCameraOverlay
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.IdentifierArgument

object CameraOverlayCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("camera-overlay") {
            literal("set") {
                argument("overlay", IdentifierArgument.id()) {
                    executes(::setOverlay)
                }
            }
            literal("clear") {
                executes(::clearOverlay)
            }
        }
    }

    private fun setOverlay(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        val overlay = IdentifierArgument.getId(context, "overlay")
        player.setCameraOverlay(overlay)
    }

    private fun clearOverlay(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        player.clearCameraOverlay()
    }
}