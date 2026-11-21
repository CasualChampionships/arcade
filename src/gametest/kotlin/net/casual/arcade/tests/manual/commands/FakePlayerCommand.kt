package net.casual.arcade.tests.manual.commands

import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.*
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.pathfinding.Path
import net.casual.arcade.utils.player.username
import net.casual.arcade.utils.server.player
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.commands.arguments.StringArgument
import net.minecraft.commands.arguments.coordinates.Vec3Argument
import net.minecraft.commands.suggestions.SuggestionContext
import net.minecraft.commands.suggestions.SuggestionProviders
import net.minecraft.server.MinecraftServer
import java.util.concurrent.CompletableFuture

@Suppress("unused")
object FakePlayerCommand: CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("fake-player") {
            argument("username", StringArgument.word()) {
                suggests(SuggestionProviders.serverOnly(::suggestFakePlayerNames))
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
                        literal("entity") {
                            argument("entity", EntityArgument.entity()) {
                                executes(::fakePlayerPathfindToEntity)
                            }
                        }
                    }
                    literal("stop") {
                        executes(::fakePlayerPathfindStop)
                    }
                    literal("info") {
                        executes(::fakePlayerPathfindInfo)
                    }
                }
            }
        }
    }

    private fun fakePlayerJoin(context: CommandContext<CommandSourceStack>): Int {
        val username = StringArgument.getString(context, "username")
        FakePlayer.join(context.source.server, username)
        return context.source.success("Joining $username")
    }

    private fun fakePlayerPathfindToPosition(context: CommandContext<CommandSourceStack>): Int {
        val player = context.fakePlayer() ?: return context.source.fail("No such fake player")
        val target = Vec3Argument.getVec3(context, "position")
        val moving = player.navigation.moveTo(target.x, target.y, target.z)
        return context.reportPath(player, moving)
    }

    private fun fakePlayerPathfindToEntity(context: CommandContext<CommandSourceStack>): Int {
        val player = context.fakePlayer() ?: return context.source.fail("No such fake player")
        val entity = EntityArgument.getEntity(context, "entity")
        val moving = player.navigation.moveTo(entity)
        return context.reportPath(player, moving)
    }

    private fun fakePlayerPathfindStop(context: CommandContext<CommandSourceStack>): Int {
        val player = context.fakePlayer() ?: return context.source.fail("No such fake player")
        player.navigation.stop()
        return context.source.success("Stopped ${player.username}")
    }

    private fun fakePlayerPathfindInfo(context: CommandContext<CommandSourceStack>): Int {
        val player = context.fakePlayer() ?: return context.source.fail("No such fake player")
        val path = player.navigation.path ?: return context.source.fail("${player.username} has no path")

        val counts = path.movements.groupingBy { it.type.id.path }.eachCount()
        val total = path.movements.sumOf { it.cost }
        return context.source.success(
            """
            ${player.username}: ${path.size} movements, ${"%.1f".format(total)} ticks
            At ${path.index}, reaches target: ${path.reachesTarget}
            ${counts.entries.joinToString { "${it.key} x${it.value}" }}
            """.trimIndent()
        )
    }

    private fun CommandContext<CommandSourceStack>.reportPath(player: FakePlayer, moving: Boolean): Int {
        if (!moving) {
            return this.source.fail("${player.username} could not find a path")
        }
        val path: Path = player.navigation.path!!
        val total = path.movements.sumOf { it.cost }
        return this.source.success(
            "${player.username} pathing: ${path.size} movements, " +
                "${"%.1f".format(total)} ticks, reaches target: ${path.reachesTarget}"
        )
    }

    private fun CommandContext<CommandSourceStack>.fakePlayer(): FakePlayer? {
        val username = StringArgument.getString(this, "username")
        return this.source.server.player(username) as? FakePlayer
    }

    private fun suggestFakePlayerNames(
        server: MinecraftServer,
        context: CommandContext<out SuggestionContext>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val names = server.playerList.players
            .filterIsInstance<FakePlayer>()
            .map { it.username }
        return SuggestionContext.suggest(names, builder)
    }
}