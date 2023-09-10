package net.casual.arcade.task

import java.util.*

interface Completable {
    val complete: Boolean

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