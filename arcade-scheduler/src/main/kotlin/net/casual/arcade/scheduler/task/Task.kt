package net.casual.arcade.scheduler.task

import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.impl.CancellableTask
import java.io.Serializable

/**
 * This interface represents a [Task] used in the
 * [TickedScheduler] which can be run.
 *
 * Tasks can be serializable, see [SavableTask],
 * or cancellable see [CancellableTask].
 *
 * @see TickedScheduler
 * @see SavableTask
 * @see CancellableTask
 */
public fun interface Task: Runnable, Serializable {
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