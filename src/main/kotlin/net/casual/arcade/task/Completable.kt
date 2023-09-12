package net.casual.arcade.task

import java.util.*

/**
 * This interface lets you append tasks to something
 * that is completable and will be completed in the
 * future.
 */
public interface Completable {
    /**
     * Whether the tasks have been completed.
     */
    public val complete: Boolean

    /**
     * Appends more tasks to the completable object.
     *
     * @param task The task to run when the object completes.
     */
    public fun then(task: Task): Completable

    public class Impl: Completable {
        private val tasks = LinkedList<Task>()
        override var complete: Boolean = false

        override fun then(task: Task): Completable {
            this.tasks.add(task)
            return this
        }

        public fun complete() {
            this.complete = true
            for (task in this.tasks) {
                task.run()
            }
        }
    }
}