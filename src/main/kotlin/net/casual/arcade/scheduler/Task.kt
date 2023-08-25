package net.casual.arcade.scheduler

abstract class Task: Runnable {
    abstract override fun run()

    private class Impl(
        val runnable: Runnable
    ): Task() {
        override fun run() {
            this.runnable.run()
        }
    }

    companion object {
        fun of(runnable: Runnable): Task {
            return Impl(runnable)
        }
    }
}