package net.casual.arcade.minigame.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.network.chat.Component

internal object PauseCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("pause") {
            requiresPermission(4)
            executes(::pauseSourceMinigame)
        }
    }

    private fun pauseSourceMinigame(context: CommandContext<CommandSourceStack>): Int {
        val minigame = context.source.player?.getMinigame() ?: context.source.level.getMinigame()
        if (minigame == null) {
            return context.source.fail(Component.translatable("minigame.command.pause.noMinigame"))
        }
        minigame.pause()
        return context.source.success(
            Component.translatable("minigame.command.pause.success", minigame.uuid.toString())
        )
    }
}