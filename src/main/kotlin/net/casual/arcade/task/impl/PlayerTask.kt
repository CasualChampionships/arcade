package net.casual.arcade.task.impl

import net.casual.arcade.task.Task
import net.casual.arcade.task.capture.CaptureConsumerTask
import net.casual.arcade.task.capture.CaptureSerializer
import net.casual.arcade.task.capture.CaptureTask
import net.casual.arcade.utils.PlayerUtils
import net.minecraft.server.level.ServerPlayer
import java.util.*

@Suppress("FunctionName")
public fun PlayerTask(player: ServerPlayer, task: CaptureConsumerTask<ServerPlayer>): Task {
    return CaptureTask<UUID, ServerPlayer>(player.uuid, PlayerUtils::player, CaptureSerializer.same(), task)
}
