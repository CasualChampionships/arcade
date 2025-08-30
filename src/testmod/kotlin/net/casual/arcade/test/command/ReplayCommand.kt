package net.casual.arcade.test.command

import net.casual.arcade.replay.command.BasicReplayCommand
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorder
import net.casual.arcade.replay.recorder.player.ReplayPlayerRecorders
import net.casual.arcade.replay.recorder.settings.SimpleRecorderSettings
import net.casual.arcade.utils.ArcadeUtils
import net.minecraft.server.level.ServerPlayer
import java.nio.file.Path
import kotlin.io.path.createDirectories

object ReplayCommand: BasicReplayCommand(ArcadeUtils.path.resolve("replays").createDirectories()) {
    override fun createPlayerRecorder(player: ServerPlayer, path: Path, format: ReplayFormat): ReplayPlayerRecorder {
        return ReplayPlayerRecorders.create(
            player, path, format, SimpleRecorderSettings.DEFAULT.copy(recordVoiceChat = true, recordHotbar = true)
        )
    }
}