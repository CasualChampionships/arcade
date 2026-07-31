/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.impl

import net.casual.arcade.scheduler.task.Cancellable
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.impl.CancellableTask.Companion.of
import net.casual.arcade.scheduler.utils.runSafely

/**
 * This extension of the [Task] interface allows
 * for cancelling of a task.
 *
 * If a task is cancelled, it will no longer run.
 *
 * This is transient, it is not serialized. If you need something which can be
 * cancelled *and* survives a restart use a [net.casual.arcade.scheduler.task.routine.Routine].
 *
 * @see Task
 */
public class CancellableTask(
    private val wrapped: Task
): Task, Cancellable {
    private val cancelled: MutableList<Task> = ArrayList()

    /**
     * Whether the task is cancelled or not.
     */
    public var isCancelled: Boolean = false
        private set

    override val isFinished: Boolean
        get() = this.isCancelled

    /**
     * This cancels the task and prevents it from running.
     */
    override fun cancel() {
        if (this.isCancelled) {
            return
        }
        this.isCancelled = true
        for (cancel in this.cancelled) {
            cancel.runSafely()
        }
        this.cancelled.clear()
    }

    /**
     * This adds a callback which will be called
     * when the task is cancelled.
     *
     * If the task has *already* been cancelled then the callback runs
     * immediately, as it would otherwise never run at all.
     *
     * @param task The task to add.
     * @return The cancellable task.
     */
    public fun ifCancelled(task: Task): CancellableTask {
        if (this.isCancelled) {
            task.runSafely()
            return this
        }
        this.cancelled.add(task)
        return this
    }

    /**
     * This makes the Cancellable's task run when
     * if the task is cancelled.
     *
     * @return The cancellable task.
     */
    public fun runIfCancelled(): CancellableTask {
        return this.ifCancelled(this.wrapped)
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

    public companion object {
        /**
         * This method creates a cancellable task with a given runnable.
         *
         * @param task The task to wrap in a cancellable task.
         * @return The cancellable task.
         */
        @JvmStatic
        public fun of(task: Task): CancellableTask {
            return CancellableTask(task)
        }

        /**
         * This method creates a [CancellableTask] with a given runnable
         * similar to the [of] method *however* this method will also
         * make the runnable be called when the task is [ifCancelled].
         *
         * @param task The task to wrap in a cancellable task.
         * @return The cancellable task.
         * @see of
         */
        @JvmStatic
        public fun cancellable(task: Task): CancellableTask {
            return of(task).runIfCancelled()
        }
    }
}
