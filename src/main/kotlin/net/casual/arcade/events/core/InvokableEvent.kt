package net.casual.arcade.events.core

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.utils.impl.Void
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This is an event that allows the user
 * to [invoke] the event. Allowing them to
 * view the result of the event.
 *
 * This invocation will only happen ***once***,
 * the result of the first invocation will be
 * stored and returned on subsequent invocations.
 * This prevents unwanted side effects when
 * multiple listeners call [invoke] on the
 * same event.
 */
public abstract class InvokableEvent<T>(
    private val recursive: Boolean = true
): CancellableEvent.Typed<T>() {
    /**
     * The result of the invocation.
     */
    private val result = lazy {
        if (this.recursive) {
            this.suppress()
        }
        this.execute()
    }

    /**
     * This method is intended to invoke
     * the event that can be cancelled.
     *
     * @return The return value of the event.
     */
    public fun invoke(): T {
        val result = this.result.value
        this.cancel(result)
        return result
    }

    /**
     * This method checks whether the
     * event has been previously invoked.
     *
     * @return Whether the event has been invoked.
     */
    public fun invoked(): Boolean {
        return this.result.isInitialized()
    }

    /**
     * This method suppresses this event from firing again
     * if broadcasted to the [GlobalEventHandler] again.
     *
     * The implementation of [execute] will most likely
     * call the method that will broadcast the same event,
     * thereby suppressing it will stop it recursively broadcasting.
     *
     * If your implementation of [execute] will not cause
     * a broadcast of this event then you should override this
     * method and remove the suppression.
     */
    private fun suppress() {
        GlobalEventHandler.suppressNextEvent(this::class.java)
    }

    /**
     * This method is implemented in the event
     * and will determine what the event will execute.
     *
     * @return the result of the event.
     */
    protected abstract fun execute(): T

    /**
     * Implementation of [InvokableEvent] however is unable
     * to be cancelled by the user of the event, only when
     * invoked.
     */
    public abstract class Uncancellable: InvokableEvent<Void>() {
        final override fun execute(): Void {
            this.call()
            return Void
        }

        /**
         * This method is implemented in the event
         * and will determine what the event will execute.
         */
        @OverrideOnly
        protected abstract fun call()
    }
}