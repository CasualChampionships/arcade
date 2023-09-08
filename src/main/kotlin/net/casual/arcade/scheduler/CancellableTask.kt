package net.casual.arcade.scheduler

interface CancellableTask: Task {
    fun isCancelled(): Boolean

    fun invoke()

    override fun run() {
        if (!this.isCancelled()) {
            this.invoke()
        }
    }
}