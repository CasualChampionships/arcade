package net.casualuhc.arcade.scheduler

class Task(
    private val task: Runnable
) {
    private var cancelled = false

    fun cancel() {
        this.cancelled = true
    }

    fun run() {
        if (!this.cancelled) {
            this.task.run()
        }
    }
}