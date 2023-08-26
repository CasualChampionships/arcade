package net.casual.arcade.scheduler

interface Task: Runnable {
    override fun run()

    private class Impl(
        val runnable: Runnable
    ): Task {
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