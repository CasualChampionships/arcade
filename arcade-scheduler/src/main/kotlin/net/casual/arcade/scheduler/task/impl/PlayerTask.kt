package net.casual.arcade.scheduler.task.impl

import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.capture.CaptureConsumerTask
import net.casual.arcade.scheduler.task.capture.CaptureSerializer
import net.casual.arcade.scheduler.task.capture.CaptureTask
import net.casual.arcade.utils.PlayerUtils.player
import net.casual.arcade.utils.ServerUtils
import net.minecraft.server.level.ServerPlayer
import java.util.*

@Suppress("FunctionName")
public fun PlayerTask(player: ServerPlayer, task: CaptureConsumerTask<ServerPlayer>): Task {
    return CaptureTask<UUID, ServerPlayer>(player.uuid, { ServerUtils.getServerOrNull()?.player(it) }, CaptureSerializer.same(), task)
}
