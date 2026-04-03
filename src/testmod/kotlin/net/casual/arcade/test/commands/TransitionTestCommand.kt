package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.withContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.scheduler.utils.asCoroutineDispatcher
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.utils.server.players
import net.casual.arcade.visuals.transition.TitledCountdown
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

@Suppress("unused")
object TransitionTestCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("transition") {
            executes(::run)
        }
    }

    private fun run(context: CommandContext<CommandSourceStack>) {
        val server = context.source.server
        val temporary = GlobalTickedScheduler.temporaryScheduler(5.Seconds)
        val countdown = TitledCountdown.titled()
        server.launch {
            withContext(temporary.asCoroutineDispatcher()) {
                countdown.transition { server.players }
            }
        }
    }
}