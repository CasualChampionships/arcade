package net.casual.arcade.events

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.core.Event
import java.util.function.Consumer

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
     * The phase depends on the event, and can be used to determine
     * when the listener is invoked, see the event implementation
     * you are listening to for more information.
     *
     * @param T The type of event.
     * @param priority The priority of your event listener.
     * @param phase The phase of the event, [DEFAULT] by default.
     * @param listener The callback which will be invoked when the event is fired.
     */
    public inline fun <reified T: Event> register(
        priority: Int = 1_000,
        phase: String = DEFAULT,
        listener: Consumer<T>
    ) {
        this.register(T::class.java, priority, phase, listener)
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