package net.casual.arcade.scheduler

interface CancellableTask: Task {
    fun isCancelled(): Boolean
}