/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.impl

import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.capture.CaptureConsumerTask
import net.casual.arcade.scheduler.task.capture.CaptureSerializer
import net.casual.arcade.scheduler.task.capture.CaptureTask
import net.casual.arcade.utils.server.ServerSingleton
import net.casual.arcade.utils.server.player
import net.minecraft.server.level.ServerPlayer

@Suppress("FunctionName")
public fun PlayerTask(player: ServerPlayer, task: CaptureConsumerTask<ServerPlayer>): Task {
    return CaptureTask(player.uuid, { ServerSingleton.getOrNull()?.player(it) }, CaptureSerializer.same(), task)
}
