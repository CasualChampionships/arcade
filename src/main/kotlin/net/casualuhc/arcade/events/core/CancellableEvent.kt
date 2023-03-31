package net.casualuhc.arcade.events.core

abstract class CancellableEvent: Event() {
    private var cancelled: Boolean = false

    protected open fun cancel() {
        this.cancelled = true
    }

    fun isCancelled(): Boolean {
        return this.cancelled
    }
}