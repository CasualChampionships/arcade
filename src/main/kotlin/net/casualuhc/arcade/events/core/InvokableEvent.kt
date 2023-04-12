package net.casualuhc.arcade.events.core

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
abstract class InvokableEvent<T>: CancellableEvent.Typed<T>() {
    /**
     * The result of the invocation.
     */
    private val result = lazy { this.execute() }

    /**
     * This method is intended to invoke
     * the event that can be cancelled.
     *
     * @return The return value of the event.
     */
    fun invoke(): T {
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
    fun invoked(): Boolean {
        return this.result.isInitialized()
    }

    /**
     * This method is implemented in the event
     * and will determine what the event will execute.
     *
     * @return the result of the event.
     */
    protected abstract fun execute(): T
}