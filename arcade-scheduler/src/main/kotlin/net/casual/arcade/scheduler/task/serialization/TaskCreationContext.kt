/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.serialization

import net.casual.arcade.scheduler.task.Task

/**
 * This interface provides the method that will be available
 * when creating a task using a [TaskFactory].
 *
 * It provides the ability to create subtasks using the
 * [getTask] method.
 *
 * @see TaskFactory
 * @see TaskSerializationContext
 */
public interface TaskCreationContext {
    /**
     * This provides the ability to get a subtasks by passing
     * the that tasks given stored id.
     *
     * Usually subtasks are written using [TaskSerializationContext.storeTask].
     *
     * @param uid The unique id of the stored task.
     * @return The task, null if it could not be got.
     */
    public fun getTask(uid: Int): Task?
}