package net.casual.arcade.test

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import org.reflections.Reflections
import org.reflections.scanners.Scanners

object ArcadeTestCommand: CommandTree {
    fun registerEvents() {
        for (subcommand in this.findSubcommandTrees()) {
            try {
                val method = subcommand::class.java.getMethod("registerEvents")
                method.invoke(subcommand)
            } catch (_: NoSuchMethodException) {

            }
        }
    }

    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("arcade-test") {
            for (subcommand in findSubcommandTrees()) {
                then(subcommand.create(buildContext))
            }
        }
    }

    private fun findSubcommandTrees(): List<CommandTree> {
        val reflections = Reflections("net.casual.arcade.test.commands", Scanners.SubTypes)
        val subcommands = reflections.getSubTypesOf(CommandTree::class.java)
            .mapNotNull { clazz -> clazz.kotlin.objectInstance }
        return subcommands
    }
}