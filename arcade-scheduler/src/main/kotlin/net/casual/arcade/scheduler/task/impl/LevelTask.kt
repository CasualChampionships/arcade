/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.impl

import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.utils.server.ServerSingleton
import net.minecraft.server.level.ServerLevel
import java.util.function.Consumer

@Suppress("FunctionName")
public fun LevelTask(level: ServerLevel, task: Consumer<ServerLevel>): Task {
    val dimension = level.dimension()
    return Task {
        val resolved = ServerSingleton.getOrNull()?.getLevel(dimension)
        if (resolved != null) {
            task.accept(resolved)
        }
    }
}
