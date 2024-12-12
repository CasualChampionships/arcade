package net.casual.arcade.events

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.events.common.Event

/**
 * This is an implementation of [ListenerProvider] which allows
 * this class to register multiple event listeners to be called
 * when an event is broadcasted.
 *
 * By default, this class in used in [GlobalEventHandler] where
 * you can register your events.
 * However, if you would like the ability to unregister your
 * events, you can create your own [SimpleListenerRegistry] and then add
 * and remove it from the [GlobalEventHandler] by using
 * [GlobalEventHandler.addProvider] and [GlobalEventHandler.removeProvider].
 *
 * You can then [register] events to the created [SimpleListenerRegistry]
 * as normal and they will be invoked when the [GlobalEventHandler]
 * broadcasts the given event.
 *
 * @see GlobalEventHandler
 * @see ListenerProvider
 */
public class SimpleListenerRegistry: ListenerRegistry {
    private val events = Reference2ObjectOpenHashMap<Class<out Event>, ArrayList<EventListener<*>>>()

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
     * Registers an event listener.
     *
     * This allows you to register a callback to a specific event type.
     * This callback will **only** fire when instances of the given type
     * are fired.
     *
     * @param T The type of event.
     * @param type The class of the event that you want to listen to.
     * @param listener The callback which will be invoked when the event is fired.
     */
    override fun <T: Event> register(type: Class<T>, listener: EventListener<T>) {
        @Suppress("UNCHECKED_CAST")
        val listeners = this.events.getOrPut(type) { ArrayList() } as MutableList<EventListener<T>>
        listeners.add(this.findIndexForPriority(listeners, listener), listener)
    }

    /**
     * Clears all event listeners from this EventHandler.
     */
    public fun clear() {
        this.events.clear()
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
}