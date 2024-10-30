package net.casual.arcade.scheduler.task.serialization

import com.google.gson.JsonObject
import net.casual.arcade.scheduler.task.Task

/**
 * This interface provides the method that will be available
 * when creating a task using a [TaskFactory].
 *
 * It provides any custom data written by the task,
 * see [getCustomData].
 *
 * It also provides the ability to create subtasks using the
 * [createTask] method.
 *
 * @see TaskFactory
 * @see TaskSerializationContext
 */
public interface TaskCreationContext {
    /**
     * This gets the custom data written
     * by the task.
     *
     * This may be an empty object.
     */
    public val data: JsonObject

    /**
     * This provides the ability to create subtasks by passing
     * the data for another [Task].
     *
     * The data at a minimum most contain the field `id` containing
     * the if of the task that it is trying to create.
     *
     * Usually subtasks are written using [TaskSerializationContext.serializeTask].
     *
     * @param uid The unique id to create another task.
     * @return The created task, null if it could not be created.
     */
    public fun createTask(uid: Int): Task?

    public fun createSubContext(data: JsonObject): TaskCreationContext {
        return Child(this, data)
    }

    private class Child(
        private val parent: TaskCreationContext,
        override val data: JsonObject
    ): TaskCreationContext {
        override fun createTask(uid: Int): Task? {
            return this.parent.createTask(uid)
        }
    }
}