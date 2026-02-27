package net.casual.arcade.test.commands

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.*
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.utils.PlayerUtils.player
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import java.util.concurrent.CompletableFuture

@Suppress("unused")
object FakePlayerCommand: CommandTree {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("fake-player") {
            argument("username", StringArgumentType.word()) {
                suggests(::suggestFakePlayerNames)
                literal("join") {
                    executes(::fakePlayerJoin)
                }
                literal("pathfind") {
                    literal("to") {
                        literal("position") {
                            argument("position", Vec3Argument.vec3()) {
                                executes(::fakePlayerPathfindToPosition)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fakePlayerJoin(context: CommandContext<CommandSourceStack>) {
        val username = StringArgumentType.getString(context, "username")
        FakePlayer.join(context.source.server, username)
    }

    private fun fakePlayerPathfindToPosition(context: CommandContext<CommandSourceStack>) {
        val username = StringArgumentType.getString(context, "username")
        val player = context.source.server.player(username) as? FakePlayer ?: return
        val target = Vec3Argument.getVec3(context, "position")
        player.navigation.moveTo(target.x, target.y, target.z, 1.0)
    }

    private fun suggestFakePlayerNames(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val names = context.source.server.playerList.players
            .filterIsInstance<FakePlayer>()
            .map { it.scoreboardName }
        return SharedSuggestionProvider.suggest(names, builder)
    }
}