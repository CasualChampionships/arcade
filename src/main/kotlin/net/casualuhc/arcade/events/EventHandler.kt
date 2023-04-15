package net.casualuhc.arcade.events

import net.casualuhc.arcade.events.core.Event
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

/**
 * Object class that is responsible for broadcasting
 * events and announcing events to registered listeners.
 *
 * @see broadcast
 * @see register
 * @see Event
 */
object EventHandler {
    private val logger = LogManager.getLogger("ArcadeEventHandler")

    private val events = HashMap<Class<*>, ArrayList<EventListener>>()
    private val stack = ArrayDeque<DeferredEvent>()

    /**
     * This broadcasts an event for all listeners.
     *
     * It is possible that listeners may **mutate** the
     * firing event, this should then be handled by the caller.
     * See the implementation details of the firing event.
     *
     * In the unlikely case that an event is fired within
     * one of its listeners it will **not** recurse and instead
     * the recursive event will simply just be logged and suppressed.
     *
     * It is also possible to register to the firing event,
     * however these listeners will be deferred and will not
     * be fired in the same event where they were registered.
     * The reasoning for this is because we cannot guarantee
     * priority preservation.
     *
     * @param event The event that is being fired.
     */
    @JvmStatic
    fun broadcast(event: Event) {
        val type = event::class.java
        val listeners = this.events[type] ?: return

        for (deferred in this.stack) {
            if (deferred.type === type) {
                this.logger.warn(
                    "Detected recursive event (type: {}), suppressing...",
                    type.simpleName
                )
                return
            }
        }

        this.stack.push(DeferredEvent(type))

        try {
            for (listener in listeners) {
                listener.consumer.accept(event)
            }
        } finally {
            val deferred = this.stack.pop()
            if (deferred.listeners.isInitialized()) {
                for (listener in deferred.listeners.value) {
                    this.register(type, listener)
                }
            }
        }
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
    @JvmStatic
    @JvmOverloads
    fun <T: Event> register(type: Class<T>, priority: Int = 1_000, listener: Consumer<T>) {
        @Suppress("UNCHECKED_CAST")
        this.register(type, EventListener(listener as Consumer<Event>, priority))
    }

    private fun <T: Event> register(type: Class<T>, listener: EventListener) {
        for (deferred in this.stack) {
            if (deferred.type === type) {
                this.logger.warn(
                    "Tried to register event inside of that event (type: {}), deferring...",
                    type.simpleName
                )
                deferred.listeners.value.add(listener)
                return
            }
        }

        val listeners = this.events.getOrPut(type) { ArrayList() }
        listeners.add(this.findIndexForPriority(listeners, listener.priority), listener)
    }

    private fun findIndexForPriority(listeners: List<EventListener>, priority: Int): Int {
        var left = 0
        var right = listeners.size - 1
        while (left <= right) {
            val mid = (left + right) / 2
            if (listeners[mid].priority == priority) {
                return mid + 1
            } else if (listeners[mid].priority < priority) {
                left = mid + 1
            } else {
                right = mid - 1
            }
        }
        return left
    }

    private class DeferredEvent(val type: Class<*>) {
        val listeners = lazy { LinkedList<EventListener>() }
    }

    private class EventListener(
        val consumer: Consumer<Event>,
        val priority: Int
    )
}