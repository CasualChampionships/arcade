package net.casual.arcade.events.core

import net.casual.arcade.utils.impl.Wrapper

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
 * It is possible for listeners to determine whether the
 * event has been cancelled by another listener though
 * the [isCancelled] event.
 *
 * @see Event
 */
sealed class CancellableEvent: Event {
    /**
     * Whether the event is cancelled.
     */
    private var cancelled = false

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

    /**
     * Abstract class of [CancellableEvent] that
     * make the [cancel] method accessible.
     */
    abstract class Default: CancellableEvent() {
        /**
         * Cancels the event.
         */
        public final override fun cancel() {
            super.cancel()
        }
    }

    /**
     * Abstract class of [CancellableEvent] that allows
     * for cancelling with a given result. This may be
     * useful when the event is wrapped around an execution
     * that has some result.
     *
     * @param T The type of the result. May be nullable.
     * @see CancellableEvent
     */
    abstract class Typed<T>: CancellableEvent() {
        /**
         * The result of the event.
         */
        private var result: Wrapper<T>? = null

        /**
         * Cancels the event.
         */
        final override fun cancel() {
            super.cancel()
        }

        /**
         * This cancels the event with a given result.
         * This will then be subsequently used instead
         * of the result that would've been given by
         * the event.
         *
         * @param result The result.
         */
        fun cancel(result: T) {
            this.result = Wrapper(result)
            this.cancel()
        }

        /**
         * This tries to get the cancelled result
         * of this event. If the user has not set
         * the result this will throw an error.
         *
         * @return The result.
         */
        fun result(): T {
            val result = this.result
            if (result === null) {
                throw IllegalStateException("Called result() when no result is present")
            }
            return result.value
        }
    }
}