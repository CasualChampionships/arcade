package net.casual.arcade.task

import java.util.*

interface Completable {
    fun then(task: Task): Completable

    class Impl: Completable {
        private val tasks = LinkedList<Task>()

        override fun then(task: Task): Completable {
            this.tasks.add(task)
            return this
        }

        fun complete() {
            for (task in this.tasks) {
                task.run()
            }
        }
    }
}