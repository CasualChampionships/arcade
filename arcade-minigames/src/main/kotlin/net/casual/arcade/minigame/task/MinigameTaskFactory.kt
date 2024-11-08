package net.casual.arcade.minigame.task

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.scheduler.task.serialization.TaskFactory
import org.jetbrains.annotations.ApiStatus.NonExtendable

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
public interface MinigameTaskFactory<M: Minigame>: TaskFactory {
    @NonExtendable
    override fun create(context: TaskCreationContext): Task {
        if (context !is MinigameTaskCreationContext<*>) {
            throw IllegalArgumentException("Cannot create minigame task without minigame")
        }
        @Suppress("UNCHECKED_CAST")
        return this.create(context as MinigameTaskCreationContext<M>)
    }

    public fun create(context: MinigameTaskCreationContext<M>): Task
}
