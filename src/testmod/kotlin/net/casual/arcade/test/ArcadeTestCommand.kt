package net.casual.arcade.test

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import net.casual.arcade.commands.CommandTree
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.decoration.ArmorStand
import org.reflections.Reflections
import org.reflections.scanners.Scanners

object ArcadeTestCommand: CommandTree<CommandSourceStack> {
    fun registerEvents() {
        for (subcommand in this.findSubcommandTrees()) {
            try {
                val type: EntityType<ArmorStand> = EntityTypes.ARMOR_STAND;
                val method = subcommand.javaClass.getMethod("registerEvents")
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

    @Suppress("UNCHECKED_CAST")
    private fun findSubcommandTrees(): List<CommandTree<CommandSourceStack>> {
        val reflections = Reflections("net.casual.arcade.test.commands", Scanners.SubTypes)
        val subcommands = reflections.getSubTypesOf(CommandTree::class.java)
            .mapNotNull { clazz -> clazz.kotlin.objectInstance as? CommandTree<CommandSourceStack> }
        return subcommands
    }
}