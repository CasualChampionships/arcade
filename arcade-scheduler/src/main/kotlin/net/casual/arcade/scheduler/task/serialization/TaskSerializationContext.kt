/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.serialization

import com.google.gson.JsonObject
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task

/**
 * This interface provides the method that will be available
 * when writing a task with [SavableTask].
 *
 * It also provides the ability to write subtasks using the
 * [serializeTask] method.
 *
 * @see TaskFactory
 * @see TaskCreationContext
 */
public interface TaskSerializationContext {
    /**
     * This method allows you to serialize a task as a
     * [JsonObject] to allow subtasks.
     *
     * This method may return null if the task cannot
     * be serialized.
     *
     * @param task The task to serialize.
     * @return The serialized task; may be null.
     */
    public fun serializeTask(task: Task): Int?
}