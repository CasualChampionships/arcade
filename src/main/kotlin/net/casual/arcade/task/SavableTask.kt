package net.casual.arcade.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.minigame.task.MinigameTaskGenerator
import net.casual.arcade.minigame.SavableMinigame
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface is used to represent a [Task] that
 * may be serialized.
 *
 * These tasks can be scheduled in a [SavableMinigame]
 * and will be saved if the minigame is suddenly closed.
 * See [SavableMinigame] documentation for more information.
 *
 * A [SavableTask] implementation should be complimented
 * by a [TaskFactory] or [MinigameTaskFactory] to be able
 * to generate the task when deserializing.
 *
 * The best way to do this is to have your companion object
 * inherit whichever factory interface you desire and
 * implementing it there:
 * ```kotlin
 * class MyTask: SavableTask {
 *     override val id = MyTask.id
 *
 *     // ...
 *
 *     companion object: TaskFactory {
 *         override val id: String = "my_task"
 *
 *         override fun create(data: JsonObject): Task {
 *             return MyTask()
 *         }
 *     }
 * }
 * ```
 *
 * Alternatively if you have a minigame task:
 * ```kotlin
 * class MyTask(private val minigame: MyMinigame): SavableTask {
 *      override val id = MyTask.id
 *
 *      // ...
 *
 *      companion object: MinigameTaskFactory<MyMinigame> {
 *          override val id: String = "my_task"
 *
 *          override fun create(minigame: MyMinigame, data: JsonObject): Task {
 *              return MyTask(minigame)
 *          }
 *      }
 * }
 * ```
 *
 * This way you can easily register your task to a [MinigameTaskGenerator]:
 * ```kotlin
 * class MyMinigame: SavableMinigame<MyMinigame>(/* ... */) {
 *
 *     init {
 *         this.addTaskFactory(MyTask)
 *
 *         // ...
 *     }
 *
 *     // ...
 * }
 * ```
 *
 * @see SavableMinigame
 */
interface SavableTask: Task {
    /**
     * The id for the task that will be serialized.
     */
    val id: String

    /**
     * This writes any additional data needed
     * to create the implementation of this task.
     *
     * This serialized data will be passed to the
     * [TaskFactory] when generating a new task.
     *
     * @param data The [JsonObject] to write additional data to.
     */
    @OverrideOnly
    fun writeData(data: JsonObject) {

    }
}


