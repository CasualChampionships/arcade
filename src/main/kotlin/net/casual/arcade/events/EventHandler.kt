package net.casual.arcade.events

import net.casual.arcade.events.core.Event
import java.util.function.Consumer

/**
 * This is an implementation of [ListenerHandler] which allows
 * this class to register multiple event listeners to be called
 * when an event is broadcasted.
 *
 * By default, this class in used in [GlobalEventHandler] where
 * you can register your events.
 * However, if you would like the ability to unregister your
 * events, you can create your own [EventHandler] and then add
 * and remove it from the [GlobalEventHandler] by using
 * [GlobalEventHandler.addHandler] and [GlobalEventHandler.removeHandler].
 *
 * You can then [register] events to the created [EventHandler]
 * as normal and they will be invoked when the [GlobalEventHandler]
 * broadcasts the given event.
 *
 * @see GlobalEventHandler
 * @see ListenerHandler
 */
class EventHandler: ListenerHandler {
    private val events = HashMap<Class<out Event>, ArrayList<EventListener<*>>>()

    /**
     * This method gets all the [EventListener]s for a given
     * [Event] type, given by [type].
     *
     * @param type The type of the [Event] to get listeners for.
     * @return The list of [EventListener]s for the given [type].
     */
    override fun <T: Event> getListenersFor(type: Class<T>): List<EventListener<*>> {
        return this.events[type] ?: emptyList()
    }

    /**
     * Registers an event listener with a given priority.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * The priority that you register the event with determines
     * in what order the listener will be invoked. Lower values
     * of [priority] will result in being invoked earlier.
     *
     * @param T The type of event.
     * @param priority The priority of your event listener.
     * @param listener The callback which will be invoked when the event is fired.
     */
    inline fun <reified T: Event> register(priority: Int = 1_000, listener: Consumer<T>) {
        this.register(T::class.java, priority, listener)
    }

    /**
     * Registers an event listener with a given priority.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * The priority that you register the event with determines
     * in what order the listener will be invoked. Lower values
     * of [priority] will result in being invoked earlier.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param priority The priority of your event listener.
     * @param listener The callback which will be invoked when the event is fired.
     */
    fun <T: Event> register(type: Class<T>, priority: Int = 1_000, listener: Consumer<T>) {
        this.register(type, EventListenerImpl(priority, listener))
    }

    fun <T: Event> register(type: Class<T>, listener: EventListener<T>) {
        @Suppress("UNCHECKED_CAST")
        val listeners = this.events.getOrPut(type) { ArrayList() } as MutableList<EventListener<T>>
        listeners.add(this.findIndexForPriority(listeners, listener), listener)
    }

    private fun <T: Event> findIndexForPriority(listeners: List<EventListener<T>>, listener: EventListener<T>): Int {
        var left = 0
        var right = listeners.size - 1
        while (left <= right) {
            val mid = (left + right) / 2
            if (listeners[mid] < listener) {
                left = mid + 1
            } else if (listeners[mid] > listener) {
                right = mid - 1
            } else {
                return mid + 1
            }
        }
        return left
    }

    private class EventListenerImpl<T: Event>(
        override val priority: Int,
        private val listener: Consumer<T>
    ): EventListener<T> {
        override fun invoke(event: T) {
            this.listener.accept(event)
        }
    }
}