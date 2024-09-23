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
     * @param uid The unique id to create another task.
     * @return The created task, null if it could not be created.
     */
    public fun createTask(uid: Int): Task?

    private class Child(
        private val parent: TaskCreationContext,
        private val data: JsonObject
    ): TaskCreationContext {
        override fun getCustomData(): JsonObject {
            return this.data
        }

        override fun createTask(uid: Int): Task? {
            return this.parent.createTask(uid)
        }
    }

    public companion object {
        public fun TaskCreationContext.withCustomData(data: JsonObject): TaskCreationContext {
            return Child(this, data)
        }
    }
}