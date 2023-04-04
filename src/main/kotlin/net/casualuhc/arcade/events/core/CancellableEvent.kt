package net.casualuhc.arcade.events.core

/**
 * This is an event that usually is fired before
 * any side effects occur and provides the ability
 * for listeners to [cancel] the event which will
 * subsequently prevent the side effects from happening.
 *
 * It is up to the event implementation to provide a
 * [cancel] method as it may need specific data in order
 * to cancel a given event. This implementation however
 * should call the `super` [cancel] method.
 *
 * It is also possible for the event to implement the
 * [invoke] method which will call the method which
 * causes the side effects. If the listener then does
 * not cancel the event these side effects may happen again.
 *
 * It is possible for listeners to determine whether the
 * event has been cancelled by another listener though
 * the [isCancelled] event.
 *
 * @see Event
 */
abstract class CancellableEvent: Event() {
    /**
     * Whether the event is cancelled.
     */
    private var cancelled: Boolean = false

    /**
     * This method is intended to invoke
     * the event that can be cancelled.
     *
     * This does **not** need to be implemented.
     *
     * @return The return value of the event.
     */
    protected open fun invoke(): Any {
        return Unit
    }

    /**
     * This method should be implemented in
     * the event implementation.
     *
     * This sets the event to being cancelled.
     */
    protected open fun cancel() {
        this.cancelled = true
    }

    /**
     * This method checks whether the given event
     * has been cancelled by a listener.
     *
     * @return Whether the event has been cancelled.
     */
    fun isCancelled(): Boolean {
        return this.cancelled
    }
}