package net.casual.arcade.test.command

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.ChunkPosArgument
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack

object ArgumentTestCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("argument-test") {
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