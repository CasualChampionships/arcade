package net.casual.arcade.minigame.task

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.scheduler.task.SavableTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.serialization.TaskCreationContext
import net.casual.arcade.scheduler.task.serialization.TaskFactory

/**
 * This class helps generate [Task]s for a minigame.
 *
 * This class allows for adding [TaskFactory]s or [MinigameTaskFactory]s.
 * Then when [generate] is called if a valid factory is present
 * it will construct a task and return it.
 *
 * This class is intended to be used with [SavableMinigame].
 *
 * @param M The type of the minigame.
 * @param minigame The owner of this task generator.
 * @see SavableMinigame
 * @see SavableTask
 */
public class MinigameTaskGenerator<M: Minigame<M>>(
    /**
     * The owner of this task generator.
     */
    private val minigame: M
) {
    private val minigameFactories = Object2ObjectOpenHashMap<String, MinigameTaskFactory<M>>()
    private val regularFactories = Object2ObjectOpenHashMap<String, TaskFactory>()

    /**
     * This tries to generate a [Task] with the given [id]
     * and [data] using the available factories.
     *
     * If no valid factory is found, then null is returned.
     *
     * @param id The id of the task.
     * @param context The task creation context.
     * @return The generated task; may be null.
     */
    public fun generate(id: String, context: TaskCreationContext): Task? {
        this.minigameFactories[id]?.let { factory ->
            return factory.create(this.minigame, context)
        }
        this.regularFactories[id]?.let { factory ->
            return factory.create(context)
        }
        return null
    }

    /**
     * This adds a [TaskFactory] to this generator.
     *
     * @param factory The factory to add.
     * @see TaskFactory
     */
    public fun addFactory(factory: TaskFactory) {
        this.regularFactories[factory.id] = factory
    }

    /**
     * This adds a [MinigameTaskFactory] to this generator.
     *
     * @param factory The factory to add.
     * @see MinigameTaskFactory
     */
    public fun addFactory(factory: MinigameTaskFactory<M>) {
        this.minigameFactories[factory.id] = factory
    }
}