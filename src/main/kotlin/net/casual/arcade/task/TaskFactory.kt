package net.casual.arcade.task

import net.casual.arcade.minigame.task.MinigameTaskFactory

/**
 * This interface is for creating tasks from serialized data.
 *
 * These should be implemented alongside a [SavableTask],
 * see [SavableTask] for more details.
 *
 * @see SavableTask
 * @see MinigameTaskFactory
 */
public interface TaskFactory {
    /**
     * The id for the task that is being generated.
     */
    public val id: String

    /**
     * This creates a [Task] from the given [data].
     *
     * @param context The task creation context.
     * @return The generated task.
     */
    public fun create(context: TaskCreationContext): Task
}