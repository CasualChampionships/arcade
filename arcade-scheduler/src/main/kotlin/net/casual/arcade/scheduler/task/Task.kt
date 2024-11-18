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
public fun interface Task: Runnable {
    /**
     * This runs the task.
     */
    override fun run()
}