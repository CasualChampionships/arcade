/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task

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
     * If the task is already complete, then this will do nothing.
     *
     * @param task The task to run when the object completes.
     *
     * @see thenOrNow
     */
    public fun then(task: Task): Completable

    public class Impl: Completable {
        private val tasks: MutableList<Task> = LinkedList()
        override var complete: Boolean = false

        override fun then(task: Task): Impl {
            if (!this.complete) {
                this.tasks.add(task)
            }
            return this
        }

        public fun tasks(): List<Task> {
            return this.tasks
        }

        public fun complete() {
            if (!this.complete) {
                this.complete = true
                for (task in this.tasks) {
                    task.run()
                }
            }
        }
    }

    public companion object {
        private val COMPLETE = object: Completable {
            override val complete: Boolean
                get() = true

            override fun then(task: Task): Completable {
                return this
            }
        }

        public fun Completable.thenOrNow(task: Task): Completable {
            if (this.complete) {
                task.run()
                return this
            }
            return this.then(task)
        }

        public fun complete(): Completable {
            return COMPLETE
        }
    }
}