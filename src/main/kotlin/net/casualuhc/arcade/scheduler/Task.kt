package net.casualuhc.arcade.scheduler

abstract class Task {
    private var cancelled = false

    fun cancel() {
        this.cancelled = true
    }

    fun run() {
        if (!this.cancelled) {
            this.invoke()
        }
    }

    protected abstract fun invoke()

    private class Impl(
        val runnable: Runnable
    ): Task() {
        override fun invoke() {
            this.runnable.run()
        }
    }

    companion object {
        fun of(runnable: Runnable): Task {
            return Impl(runnable)
        }
    }
}