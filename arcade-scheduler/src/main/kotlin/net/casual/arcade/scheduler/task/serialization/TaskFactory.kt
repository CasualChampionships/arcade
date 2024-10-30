package net.casual.arcade.scheduler.task.serialization

import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.impl.CancellableTask
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceLocation

/**
 * This interface is for creating tasks from serialized data.
 *
 * These should be implemented alongside a [SavableTask],
 * see [SavableTask] for more details.
 *
 * @see SavableTask
 */
public interface TaskFactory {
    /**
     * The id for the task that is being generated.
     */
    public val id: ResourceLocation

    /**
     * This creates a [Task] from the given [data].
     *
     * @param context The task creation context.
     * @return The generated task.
     */
    public fun create(context: TaskCreationContext): Task

    public companion object {
        public fun bootstrap(registry: Registry<TaskFactory>) {
            Registry.register(registry, CancellableTask.Savable.id, CancellableTask.Savable)
        }
    }
}