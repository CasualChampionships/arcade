package net.casual.arcade.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame

/**
 * This interface is for creating tasks from serialized data.
 *
 * This interface is similar to [TaskFactory] however it
 * allows for creating minigame-owned tasks, for tasks
 * that need to directly reference their minigame.
 *
 * These should be implemented alongside a [SavableTask],
 * see [SavableTask] for more details.
 *
 * @see SavableTask
 * @see TaskFactory
 */
interface MinigameTaskFactory<T: Minigame> {
    /**
     * The id for the task that is being generated.
     */
    val id: String

    /**
     * This creates a [Task] from the given [data] and
     * the task's [minigame] owner.
     *
     * @param minigame The owner of this task.
     * @param data The serialized data.
     * @return The generated task.
     */
    fun create(minigame: T, data: JsonObject): Task
}