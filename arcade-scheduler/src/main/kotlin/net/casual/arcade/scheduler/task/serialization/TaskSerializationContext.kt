/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.serialization

import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task

/**
 * This interface provides the method that will be available
 * when writing a task with [SavableTask].
 *
 * It also provides the ability to write subtasks using the
 * [storeTask] method.
 *
 * @see TaskFactory
 * @see TaskCreationContext
 */
public interface TaskSerializationContext {
    /**
     * This method allows you to store a task and
     * returns its respective stored id.
     *
     * @param task The task to store.
     * @return The store task id.
     */
    public fun storeTask(task: Task): Int
}