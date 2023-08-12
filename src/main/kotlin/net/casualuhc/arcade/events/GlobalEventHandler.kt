package net.casualuhc.arcade.events

import net.casualuhc.arcade.events.GlobalEventHandler.broadcast
import net.casualuhc.arcade.events.GlobalEventHandler.addHandler
import net.casualuhc.arcade.events.core.Event
import net.casualuhc.arcade.utils.CollectionUtils.addSorted
import org.apache.logging.log4j.LogManager
import java.util.*
import java.util.function.Consumer

/**
 * Object class that is responsible for broadcasting
 * events and announcing events to registered listeners.
 *
 * @see broadcast
 * @see addHandler
 * @see Event
 */
object GlobalEventHandler {
    private val logger = LogManager.getLogger("ArcadeEventHandler")

    private val stack = ArrayDeque<Class<out Event>>()
    private val handlers = HashSet<EventHandler>()
    private val handler = EventHandler()

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
    fun <T: Event> broadcast(event: T) {
        val type = event::class.java
        @Suppress("UNCHECKED_CAST")
        val listeners = ArrayList(this.handler.getListenersFor(type)) as MutableList<EventListener<T>>

        for (within in this.stack) {
            if (within === type) {
                this.logger.warn(
                    "Detected recursive event (type: {}), suppressing...",
                    type.simpleName
                )
                return
            }
        }

        try {
            this.stack.push(type)

            for (handler in this.handlers) {
                @Suppress("UNCHECKED_CAST")
                listeners.addSorted(handler.getListenersFor(type) as List<EventListener<T>>)
            }

            for (listener in listeners) {
                listener.invoke(event)
            }
        } finally {
            this.stack.pop()
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
        this.handler.register(type, priority, listener)
    }

    @JvmStatic
    fun addHandler(handler: EventHandler) {
        this.handlers.add(handler)
    }

    @JvmStatic
    fun removeHandler(handler: EventHandler) {
        this.handlers.remove(handler)
    }
}
