package net.casual.arcade.task

import java.util.*

/**
 * This interface lets you append tasks to something
 * that is completable and will be completed in the
 * future.
 */
interface Completable {
    /**
     * Whether the tasks have been completed.
     */
    val complete: Boolean

    /**
     * Appends more tasks to the completable object.
     *
     * @param task The task to run when the object completes.
     */
    fun then(task: Task): Completable

    class Impl: Completable {
        private val tasks = LinkedList<Task>()
        override var complete = false

        override fun then(task: Task): Completable {
            this.tasks.add(task)
            return this
        }

        fun complete() {
            this.complete = true
            for (task in this.tasks) {
                task.run()
            }
        }
    }
}