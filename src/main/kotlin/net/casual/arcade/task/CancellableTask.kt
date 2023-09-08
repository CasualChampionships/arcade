package net.casual.arcade.task

import org.jetbrains.annotations.ApiStatus.NonExtendable

/**
 * This extension of the [Task] interface allows
 * for cancelling of a task.
 *
 * If a task is cancelled, it will no longer run
 * or serialized (if it also implements [SavableTask]).
 *
 * @see Task
 */
interface CancellableTask: Task {
    /**
     * Checks whether the task is cancelled.
     *
     * @return Whether the task is cancelled.
     */
    fun isCancelled(): Boolean

    /**
     * This runs the task.
     */
    fun invoke()

    /**
     * This will be called when running the task,
     * however, it is overridden to first check whether
     * this task is cancelled before invoking [invoke].
     *
     * This is where the task should now implement its
     * [run] logic instead of here.
     * This method should **NOT** be overridden.
     *
     * @see invoke
     */
    @NonExtendable
    override fun run() {
        if (!this.isCancelled()) {
            this.invoke()
        }
    }
}