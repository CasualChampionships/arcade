/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.task

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext

/**
 * This is an extension to [TaskCreationContext] which provides context
 * for which [minigame] is constructing the current task.
 *
 * @param M The minigame type.
 * @see TaskCreationContext
 */
public interface MinigameTaskCreationContext<M: Minigame>: TaskCreationContext {
    /**
     * The minigame instance constructing tasks.
     */
    public val minigame: M
}