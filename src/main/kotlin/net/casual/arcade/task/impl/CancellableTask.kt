package net.casual.arcade.task.impl

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.task.*
import net.casual.arcade.task.serialization.TaskCreationContext
import net.casual.arcade.task.serialization.TaskFactory
import net.casual.arcade.task.serialization.TaskWriteContext
import net.casual.arcade.utils.JsonUtils
import net.casual.arcade.utils.JsonUtils.boolean
import net.casual.arcade.utils.JsonUtils.objects

/**
 * This extension of the [Task] interface allows
 * for cancelling of a task.
 *
 * If a task is cancelled, it will no longer run
 * or serialized.
 *
 * @see Task
 */
public sealed class CancellableTask(
    protected val wrapped: Task
): Task {
    protected val cancelled: MutableList<Task> = ArrayList()

    /**
     * Whether the task is cancelled or not.
     */
    public var isCancelled: Boolean = false
        private set

    /**
     * This cancels the task and prevents it from running.
     */
    public fun cancel() {
        if (this.isCancelled) {
            return
        }
        this.isCancelled = true
        for (cancel in this.cancelled) {
            cancel.run()
        }
    }

    /**
     * This adds a callback which will be called
     * when the task is cancelled.
     *
     * @param task The task to add.
     * @return The cancellable task.
     */
    public fun cancelled(task: Task): CancellableTask {
        this.cancelled.add(task)
        return this
    }

    /**
     * This makes the Cancellable's task run when
     * if the task is cancelled.
     *
     * @return The cancellable task.
     */
    public fun runOnCancel(): CancellableTask {
        return this.cancelled(this.wrapped)
    }

    /**
     * This will be called when running the task,
     * however, it will check whether the event has
     * been cancelled before running the wrapped
     * task, if the current task is cancelled then
     * it will not run the wrapped task.
     */
    override fun run() {
        if (!this.isCancelled) {
            this.wrapped.run()
        }
    }

    internal class Savable(wrapped: Task): CancellableTask(wrapped), SavableTask {
        override val id = Companion.id

        override fun writeCustomData(context: TaskWriteContext): JsonObject {
            val data = JsonObject()
            val wrappedData = context.writeTask(this.wrapped)
            if (wrappedData == null) {
                val message = "Cancellable\$Savable task failed to write wrapped task ${this.wrapped::class.simpleName}"
                throw IllegalStateException(message)
            }
            data.add("wrapped", wrappedData)
            val onCancel = JsonArray()
            for (cancel in this.cancelled) {
                val onCancelData = context.writeTask(cancel)
                if (onCancelData == null) {
                    val message = "Cancellable\$Savable task failed to write on_cancel task ${cancel::class.simpleName}"
                    throw IllegalStateException(message)
                }
                onCancel.add(onCancelData)
            }
            data.add("on_cancel", onCancel)
            data.addProperty("is_cancelled", this.isCancelled)
            return data
        }

        companion object: TaskFactory {
            override val id = "$${Arcade.MOD_ID}_internal_savable_cancellable"

            override fun create(context: TaskCreationContext): Task {
                val data = context.getCustomData()
                val wrappedData = data.getAsJsonObject("wrapped")
                val wrapped = context.createTask(wrappedData)
                if (wrapped == null) {
                    val message = "Cancellable\$Savable task failed to create wrapped task with data: ${JsonUtils.GSON.toJson(wrappedData)}"
                    throw IllegalStateException(message)
                }
                val isCancelled = data.boolean("is_cancelled")

                val savable = Savable(wrapped)
                if (isCancelled) {
                    savable.cancel()
                }

                val onCancelArray = data.getAsJsonArray("on_cancel")
                for (onCancelData in onCancelArray.objects()) {
                    val task = context.createTask(onCancelData)
                    if (task == null) {
                        val message = "Cancellable\$Savable task failed to create on_cancel task with data ${JsonUtils.GSON.toJson(onCancelData)}"
                        throw IllegalStateException(message)
                    }
                    savable.cancelled(task)
                }
                return savable
            }
        }
    }

    public companion object {
        /**
         * This method creates a cancellable task with a given runnable.
         *
         * If given a savable task this will save the savable task within
         * the cancellable task which will also be savable.
         *
         * @param task The task to wrap in a cancellable task.
         * @return The cancellable task.
         */
        @JvmStatic
        public fun of(task: Task): CancellableTask {
            return Savable(task)
        }

        /**
         * This method creates a [CancellableTask] with a given runnable
         * similar to the [of] method *however* this method will also
         * make the runnable be called when the task is [cancelled].
         *
         * @param runnable The task to wrap in a cancellable task.
         * @return The cancellable task.
         * @see of
         */
        @JvmStatic
        public fun cancellable(task: Task): CancellableTask {
            return of(task).runOnCancel()
        }
    }
}