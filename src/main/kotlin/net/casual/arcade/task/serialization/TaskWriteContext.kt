package net.casual.arcade.task.serialization

import com.google.gson.JsonObject
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.task.Task

/**
 * This interface provides the method that will be available
 * when writing a task with [SavableTask].
 *
 * It also provides the ability to write subtasks using the
 * [writeTask] method.
 *
 * @see TaskFactory
 * @see MinigameTaskFactory
 * @see TaskCreationContext
 */
public interface TaskWriteContext {
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
    public fun writeTask(task: Task): JsonObject?
}