package net.casual.arcade.task

import net.casual.arcade.scheduler.TickedScheduler

/**
 * This interface represents a [Task] used in the
 * [TickedScheduler] which can be run.
 *
 * You can create a [Task] from a plain [Runnable]
 * using the [Task.of] method.
 *
 * Tasks can be serializable, see [SavableTask],
 * or cancellable see [CancellableTask].
 *
 * @see TickedScheduler
 * @see SavableTask
 * @see CancellableTask
 */
public fun interface Task: Runnable {
    /**
     * This runs the task.
     */
    override fun run()

    public companion object {
        /**
         * Converts a [Runnable] to a [Task].
         *
         * If the runnable is a task, it just returns itself.
         * Otherwise, it will wrap it in a [Task] object.
         *
         * @param runnable The runnable to convert.
         * @return The converted task.
         */
        @JvmStatic
        public fun of(runnable: Runnable): Task {
            return if (runnable is Task) runnable else Task { runnable.run() }
        }
    }
}