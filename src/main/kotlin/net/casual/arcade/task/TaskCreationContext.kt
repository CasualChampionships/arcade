package net.casual.arcade.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.task.MinigameTaskFactory

/**
 * This interface provides the method that will be available
 * when creating a task using a [TaskFactory] or [MinigameTaskFactory].
 *
 * It provides any custom data that was written by the task,
 * see [getCustomData].
 *
 * It also provides the ability to create subtasks using the
 * [createTask] method.
 *
 * @see TaskFactory
 * @see MinigameTaskFactory
 * @see TaskWriteContext
 */
public interface TaskCreationContext {
    /**
     * This gets the custom data that was written
     * by the task.
     *
     * This may be an empty object.
     *
     * @return The serialized [JsonObject].
     */
    public fun getCustomData(): JsonObject

    /**
     * This provides the ability to create subtasks by passing
     * the data for another [Task].
     *
     * The data at a minimum most contain the field `id` containing
     * the if of the task that it is trying to create.
     *
     * Usually subtasks are written using [TaskWriteContext.writeTask].
     *
     * @param data The data to create another task.
     * @return The created task, null if it could not be created.
     */
    public fun createTask(data: JsonObject): Task?
}