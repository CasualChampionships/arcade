package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.CommandTree
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.utils.entity.teleportTo
import net.casual.arcade.utils.math.location.Location
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.world.level.dimension.BuiltinDimensionTypes

@Suppress("unused")
object DimensionCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("dimension") {
            literal("create") {
                literal("temporary") {
                    literal("void") {
                        executes(::createTemporaryVoid)
                    }
                }
            }
        }
    }

    private fun createTemporaryVoid(context: CommandContext<CommandSourceStack>): Int {
        val level = context.source.server.addCustomLevel {
            randomDimensionKey()
            dimensionType(BuiltinDimensionTypes.OVERWORLD)
            chunkGenerator(VoidChunkGenerator(context.source.server))
            persistence(LevelPersistence.Temporary)
        }

        val player = context.source.player
        player?.teleportTo(Location.DEFAULT.with(level))
        return context.source.success("Successfully generated void dimension")
    }
}