package net.casual.arcade.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.SavableMinigame

/**
 * This class helps generate [Task]s for a minigame.
 *
 * This class allows for adding [TaskFactory]s or [MinigameTaskFactory]s.
 * Then when [generate] is called if a valid factory is present
 * it will construct a task and return it.
 *
 * This class is intended to be used with [SavableMinigame], see
 * [SavableMinigame.createTask] for more information.
 *
 * @param T The type of the minigame.
 * @param minigame The owner of this task generator.
 * @see SavableMinigame
 * @see SavableMinigame.createTask
 * @see SavableTask
 */
class MinigameTaskGenerator<T: Minigame>(
    /**
     * The owner of this task generator.
     */
    private val minigame: T
) {
    private val minigameFactories = HashMap<String, MinigameTaskFactory<T>>()
    private val regularFactories = HashMap<String, TaskFactory>()

    /**
     * This tries to generate a [Task] with the given [id]
     * and [data] using the available factories.
     *
     * If no valid factory is found, then null is returned.
     *
     * @param id The id of the task.
     * @param data The data for the task.
     * @return The generated task; may be null.
     * @see SavableMinigame.createTask
     */
    fun generate(id: String, data: JsonObject): Task? {
        this.minigameFactories[id]?.let { factory ->
            return factory.create(this.minigame, data)
        }
        this.regularFactories[id]?.let { factory ->
            return factory.create(data)
        }
        return null
    }

    /**
     * This adds a [TaskFactory] to this generator.
     *
     * @param factory The factory to add.
     * @see TaskFactory
     */
    fun addFactory(factory: TaskFactory) {
        this.regularFactories[factory.id] = factory
    }

    /**
     * This adds a [MinigameTaskFactory] to this generator.
     *
     * @param factory The factory to add.
     * @see MinigameTaskFactory
     */
    fun addFactory(factory: MinigameTaskFactory<T>) {
        this.minigameFactories[factory.id] = factory
    }
}