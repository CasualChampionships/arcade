package net.casual.arcade.task

import com.google.gson.JsonObject
import net.casual.arcade.minigame.serialization.SavableMinigame
import net.casual.arcade.minigame.task.MinigameTaskFactory
import net.casual.arcade.minigame.task.MinigameTaskGenerator
import net.casual.arcade.task.serialization.TaskWriteContext
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
 *     override fun fun writeCustomData(context: TaskWriteContext): JsonObject {
 *         return JsonObject()
 *     }
 *
 *     companion object: TaskFactory {
 *         override val id: String = "my_task"
 *
 *         override fun create(context: TaskCreationContext): Task {
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
 *      override fun fun writeCustomData(context: TaskWriteContext): JsonObject {
 *          return JsonObject()
 *      }
 *
 *      companion object: MinigameTaskFactory<MyMinigame> {
 *          override val id: String = "my_task"
 *
 *          override fun create(minigame: MyMinigame, context: TaskCreationContext): Task {
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
public interface SavableTask: Task {
    /**
     * The id for the task that will be serialized.
     */
    public val id: String

    /**
     * This writes any additional data needed
     * to create the implementation of this task.
     *
     * This serialized data will be passed to the
     * [TaskFactory] when generating a new task.
     *
     * @param context The [TaskWriteContext].
     * @return The serialized data.
     */
    @OverrideOnly
    public fun writeCustomData(context: TaskWriteContext): JsonObject {
        return JsonObject()
    }
}


