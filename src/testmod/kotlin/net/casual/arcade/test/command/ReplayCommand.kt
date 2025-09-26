package net.casual.arcade.test.command

import com.mojang.brigadier.arguments.StringArgumentType
import com.mojang.brigadier.builder.LiteralArgumentBuilder
import com.mojang.brigadier.context.CommandContext
import net.casual.arcade.commands.argument
import net.casual.arcade.commands.literal
import net.casual.arcade.commands.success
import net.casual.arcade.replay.command.BasicReplayCommand
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.replay.recorder.settings.SimpleRecorderSettings
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.utils.ResourcePackUtils.sendResourcePack
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.network.ResolvableURL
import net.minecraft.commands.CommandBuildContext
import net.minecraft.commands.CommandSourceStack
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path
import kotlin.io.path.createDirectories

object ReplayCommand: BasicReplayCommand(ArcadeUtils.path.resolve("replays").createDirectories()) {
    override fun create(buildContext: CommandBuildContext): LiteralArgumentBuilder<CommandSourceStack> {
        return super.create(buildContext).apply {
            literal("resource-pack") {
                literal("push") {
                    argument("url", StringArgumentType.string()) {
                        executes(::pushResourcePack)
                    }
                }
            }
        }
    }

    override fun createPlayerRecorder(player: ServerPlayer, path: Path, format: ReplayFormat): ReplayPlayerRecorder {
        return ReplayPlayerRecorders.create(
            player, path, format, SimpleRecorderSettings.DEFAULT.copy(recordVoiceChat = true, recordHotbar = true)
        )
    }

    private fun pushResourcePack(context: CommandContext<CommandSourceStack>): Int {
        val url = StringArgumentType.getString(context, "url")
        val player = context.source.playerOrException
        player.sendResourcePack(PackInfo(ResolvableURL.from(url), "", false, null))
        return context.source.success("Successfully sent resource pack")
    }
}