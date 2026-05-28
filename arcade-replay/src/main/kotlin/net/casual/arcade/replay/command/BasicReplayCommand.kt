/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.command

import com.mojang.brigadier.arguments.IntegerArgumentType
import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import net.casual.arcade.commands.*
import net.casual.arcade.commands.arguments.EnumArgument
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.recorder.chunk.ChunkArea
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorder
import net.casual.arcade.replay.recorder.chunk.ReplayChunkRecorders
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.replay.util.FileUtils.streamDirectoryEntriesOrEmpty
import net.casual.arcade.replay.viewer.ReplayViewers
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.commands.SharedSuggestionProvider
import net.minecraft.commands.arguments.EntityArgument
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path
import java.util.concurrent.CompletableFuture
import kotlin.io.path.name

public open class BasicReplayCommand(
    protected val path: Path
): CommandTree<CommandSourceStack> {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return CommandTree.buildLiteral("replay") {
            literal("start") {
                literal("player") {
                    argument("player", EntityArgument.player()) {
                        argument("format", EnumArgument.enumeration<ReplayFormat> { it.id() }) {
                            executes(::startPlayerReplay)
                        }
                    }
                }
                literal("chunks") {
                    literal("around") {
                        argument("x", IntegerArgumentType.integer()) {
                            suggests(::suggestChunkX)
                            argument("z", IntegerArgumentType.integer()) {
                                suggests(::suggestChunkZ)
                                argument("radius", IntegerArgumentType.integer()) {
                                    argument("format", EnumArgument.enumeration<ReplayFormat> { it.id() }) {
                                        executes(::startChunksAround)
                                    }
                                }
                            }
                        }
                    }
                }
            }
            literal("pause") {
                literal("all") {
                    executes(::pauseAllReplays)
                }
            }
            literal("resume") {
                literal("all") {
                    executes(::resumeAllReplays)
                }
            }
            literal("stop") {
                literal("player") {
                    argument("player", EntityArgument.player()) {
                        executes(::stopPlayerReplay)
                    }
                }
                literal("all") {
                    executes(::stopAllReplays)
                }
            }
            literal("view") {
                argument("name", StringArgumentType.string()) {
                    suggests(::suggestViewableReplays)
                    executes(::viewReplay)
                }
            }
            literal("status") {
                executes(::queryReplayStatuses)
            }
            literal("marker") {
                literal("players") {
                    argument("players", EntityArgument.players()) {
                        argument("name", StringArgumentType.string()) {
                            executes(::addMarkersToPlayerRecorders)
                        }
                    }
                }
            }
        }
    }

    protected open fun createPlayerRecorder(player: ServerPlayer, path: Path, format: ReplayFormat): ReplayPlayerRecorder {
        return ReplayPlayerRecorders.create(player, path, format)
    }

    protected open fun createChunkRecorder(area: ChunkArea, path: Path, format: ReplayFormat): ReplayChunkRecorder {
        return ReplayChunkRecorders.create(area, path, format)
    }

    private fun startPlayerReplay(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        val format = EnumArgument.getEnumeration<ReplayFormat>(context, "format")
        val recorder = this.createPlayerRecorder(player, this.path, format)
        try {
            recorder.start()
        } catch (e: Exception) {
            ArcadeUtils.logger.error("Failed to start replay", e)
            return context.source.fail("Failed to start replay")
        }
        return context.source.success("Successfully started recorder ${recorder.getName()}")
    }

    private fun startChunksAround(context: CommandContext<CommandSourceStack>): Int {
        val level = context.source.level
        val chunkX = IntegerArgumentType.getInteger(context, "x")
        val chunkZ = IntegerArgumentType.getInteger(context, "z")
        val radius = IntegerArgumentType.getInteger(context, "radius")
        val format = EnumArgument.getEnumeration<ReplayFormat>(context, "format")

        val area = ChunkArea.of(level, chunkX, chunkZ, radius)
        val recorder = this.createChunkRecorder(area, this.path, format)
        try {
            recorder.start()
        } catch (e: Exception) {
            ArcadeUtils.logger.error("Failed to start replay", e)
            return context.source.fail("Failed to start replay")
        }
        return context.source.success("Successfully started recorder ${recorder.getName()}")
    }

    private fun pauseAllReplays(context: CommandContext<CommandSourceStack>): Int {
        ReplayPlayerRecorders.recorders().forEach(ReplayRecorder::pause)
        ReplayChunkRecorders.recorders().forEach(ReplayRecorder::pause)
        return context.source.success("Successfully paused all recorders")
    }

    private fun resumeAllReplays(context: CommandContext<CommandSourceStack>): Int {
        ReplayPlayerRecorders.recorders().forEach(ReplayRecorder::resume)
        ReplayChunkRecorders.recorders().forEach(ReplayRecorder::resume)
        return context.source.success("Successfully resumed all recorders")
    }

    private fun stopPlayerReplay(context: CommandContext<CommandSourceStack>): Int {
        val player = EntityArgument.getPlayer(context, "player")
        ReplayPlayerRecorders.stop(player.uuid)
        return context.source.success("Successfully stopped all recorders for ${player.scoreboardName}")
    }

    private fun stopAllReplays(context: CommandContext<CommandSourceStack>): Int {
        ReplayPlayerRecorders.recorders().forEach(ReplayRecorder::stop)
        ReplayChunkRecorders.recorders().forEach(ReplayRecorder::stop)
        return context.source.success("Successfully stopped all recorders")
    }

    private fun viewReplay(context: CommandContext<CommandSourceStack>) {
        val name = StringArgumentType.getString(context, "name")
        val path = this.path.resolve(name)
        try {
            val viewer = ReplayViewers.create(path, context.source.playerOrException)
            viewer.start()
        } catch (e: Exception) {
            ArcadeUtils.logger.error("Failed to view replay", e)
        }
    }

    private fun queryReplayStatuses(context: CommandContext<CommandSourceStack>): Int {
        val builder = StringBuilder("Replay Status:\n")

        val players = getStatusFor("Players", ReplayPlayerRecorders.recorders())
        val chunks = getStatusFor("Chunks", ReplayChunkRecorders.recorders())
        val closing = listOf(ReplayPlayerRecorders.closing(), ReplayChunkRecorders.closing()).flatten()

        for (player in players) {
            builder.append("${player}\n")
        }
        for (chunk in chunks) {
            builder.append("${chunk}\n")
        }
        if (closing.isNotEmpty()) {
            builder.append("Currently Saving:\n")
            for (saving in closing) {
                builder.append("${saving.getName()}\n")
            }
        }

        return context.source.success(builder.removeSuffix("\n").toString())
    }

    private fun addMarkersToPlayerRecorders(context: CommandContext<CommandSourceStack>) {
        val players = EntityArgument.getPlayers(context, "players")
        val name = StringArgumentType.getString(context, "name")
        for (player in players) {
            ReplayPlayerRecorders.get(player).forEach { recorder ->
                recorder.addMarker(name)
            }
        }
    }

    private fun getStatusFor(type: String, recorders: Collection<ReplayRecorder>): List<String> {
        if (recorders.isNotEmpty()) {
            val lines = ArrayList<String>()
            lines.add("Currently Recording $type:")
            for (recorder in recorders) {
                lines.add(recorder.getStatus())
            }
            return lines
        }
        return listOf("Not Currently Recording $type")
    }

    private fun suggestViewableReplays(
        @Suppress("UnusedParameter") context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val suggestions = this.path.streamDirectoryEntriesOrEmpty()
            .filter { path -> ReplayFormat.formatOf(path) != null }
            .map { "\"${it.name}\"" }
        return SharedSuggestionProvider.suggest(suggestions, builder)
    }

    private fun suggestChunkX(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val x = context.source.playerOrException.chunkPosition().x
        return SharedSuggestionProvider.suggest(listOf(x.toString()), builder)
    }

    private fun suggestChunkZ(
        context: CommandContext<CommandSourceStack>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val z = context.source.playerOrException.chunkPosition().z
        return SharedSuggestionProvider.suggest(listOf(z.toString()), builder)
    }
}