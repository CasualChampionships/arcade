package net.casual.arcade.tests.manual.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import kotlinx.coroutines.launch
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.scheduler.GlobalTickedScheduler
import net.casual.arcade.utils.TimeUtils.Seconds
import net.casual.arcade.utils.server.players
import net.casual.arcade.virtual.visuals.transition.TitledCountdown
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
        val temporary = GlobalTickedScheduler.Server.temporaryScheduler(5.Seconds)
        val countdown = TitledCountdown.titled()
        temporary.asCoroutineScope().launch {
            countdown.transition { server.players }
        }
    }
}