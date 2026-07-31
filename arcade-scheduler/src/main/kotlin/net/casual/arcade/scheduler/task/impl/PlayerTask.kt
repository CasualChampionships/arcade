/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.impl

import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.server.ServerSingleton
import net.casual.arcade.utils.server.player
import net.minecraft.server.level.ServerPlayer
import java.util.function.Consumer

@Suppress("FunctionName")
public fun PlayerTask(player: ServerPlayer, task: Consumer<ServerPlayer>): Task {
    val uuid = player.uuid
    return Task {
        val resolved = ServerSingleton.getOrNull()?.player(uuid)
        if (resolved != null) {
            task.accept(resolved)
        }
    }
}
