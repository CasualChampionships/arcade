package net.casual.arcade.minigame.task

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.task.SavableTask
import net.casual.arcade.task.Task
import net.casual.arcade.task.TaskCreationContext
import net.casual.arcade.task.TaskFactory

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
public interface MinigameTaskFactory<M: Minigame<M>> {
    /**
     * The id for the task that is being generated.
     */
    public val id: String

    /**
     * This creates a [Task] from the given [data] and
     * the task's [minigame] owner.
     *
     * @param minigame The owner of this task.
     * @param context The task creation context.
     * @return The generated task.
     */
    public fun create(minigame: M, context: TaskCreationContext): Task
}
