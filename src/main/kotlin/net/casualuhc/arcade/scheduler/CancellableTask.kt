package net.casualuhc.arcade.scheduler

@Suppress("unused")
class CancellableTask private constructor(
    private val task: Runnable
): Task() {
    private var cancelled = false

    fun cancel() {
        this.cancelled = true
    }

    override fun run() {
        if (!this.cancelled) {
            this.task.run()
        }
    }

    companion object {
        fun of(runnable: Runnable): CancellableTask {
            return CancellableTask(runnable)
        }
    }
}