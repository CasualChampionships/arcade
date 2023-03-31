package net.casualuhc.arcade.scheduler

class Task(
    private val task: () -> Unit
) {
    private var cancelled = false

    fun cancel() {
        this.cancelled = true
    }

    fun run() {
        if (!this.cancelled) {
            this.task()
        }
    }
}