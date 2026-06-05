package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.executes
import net.casual.arcade.commands.literal
import net.casual.arcade.test.guis.dialog.TestDialog
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.core.Holder

@Suppress("unused")
object DialogCommand: CommandTree<CommandSourceStack> {
    fun registerEvents() {
        TestDialog.registerEvents()
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("dialog") {
            literal("test") {
                executes(::openTestDialog)
            }
        }
    }

    private fun openTestDialog(context: CommandContext<CommandSourceStack>) {
        val player = context.source.playerOrException
        player.openDialog(Holder.direct(TestDialog.create()))
    }
}