package net.casual.arcade.test.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.ChunkPosArgument
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

@Suppress("unused")
object CommandArgumentsCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("command-arguments") {
            literal("chunk-pos") {
                argument("pos", ChunkPosArgument.position()) {
                    executes(::outputChunkPos)
                }
            }
        }
    }

    private fun outputChunkPos(context: CommandContext<CommandSourceStack>) {
        val pos = ChunkPosArgument.getPosition(context, "pos")
        context.source.success("Chunk position of $pos")
    }
}