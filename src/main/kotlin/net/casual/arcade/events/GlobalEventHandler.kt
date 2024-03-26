package net.casual.arcade.events

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.casual.arcade.Arcade
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.GlobalEventHandler.addHandler
import net.casual.arcade.events.GlobalEventHandler.broadcast
import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.server.SafeServerlessEvent
import net.casual.arcade.events.server.ServerOffThreadEvent
import net.casual.arcade.utils.CollectionUtils.addSorted
import org.apache.logging.log4j.LogManager
import java.util.function.Consumer

/**
 * Object class that is responsible for broadcasting
 * events and announcing events to registered listeners.
 *
 * @see broadcast
 * @see addHandler
 * @see Event
 */
public object GlobalEventHandler {
    private const val MAX_RECURSIONS = 10

    private val logger = LogManager.getLogger("ArcadeEventHandler")

    private val suppressed = HashSet<Class<out Event>>()
    private val stack = Object2IntOpenHashMap<Class<out Event>>()
    private val handlers = HashSet<ListenerHandler>()
    private val handler = EventHandler()

    private var stopping = false

    /**
     * This broadcasts an event for all listeners.
     *
     * It is possible that listeners may **mutate** the
     * firing event, the caller should then handle this.
     * See the implementation details of the firing event.
     *
     * In the unlikely case that an event is fired within
     * one of its listeners, it will **not** recurse, and instead
     * the recursive event will simply just be logged and suppressed.
     *
     * It is also possible to register to the firing event,
     * however, these listeners will be deferred and will not
     * be fired in the same event where they were registered.
     * The reasoning for this is because we cannot guarantee
     * priority preservation.
     *
     * @param event The event that is being fired.
     */
    @JvmStatic
    @JvmOverloads
    public fun <T: Event> broadcast(event: T, phases: Set<String> = BuiltInEventPhases.DEFAULT_PHASES) {
        val type = event::class.java

        if (this.checkThread(event, type)) {
            return
        }

        if (this.suppressed.remove(type)) {
            this.logger.debug("Suppressing event (type: {})", type.simpleName)
            return
        }

        if (this.checkRecursive(type)) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        val listeners = ArrayList(this.handler.getListenersFor(type)) as MutableList<EventListener<T>>
        try {
            this.stack.addTo(type, 1)

            for (handler in this.handlers) {
                @Suppress("UNCHECKED_CAST")
                listeners.addSorted(handler.getListenersFor(type) as List<EventListener<T>>)
            }

            for (listener in listeners) {
                if (phases.contains(listener.phase)) {
                    listener.invoke(event)
                }
            }
        } finally {
            this.stack.addTo(type, -1)
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
    public inline fun <reified T: Event> register(priority: Int = 1_000, phase: String = DEFAULT, listener: Consumer<T>) {
        this.register(T::class.java, priority, phase, listener)
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
    public fun <T: Event> register(type: Class<T>, priority: Int = 1_000, phase: String = DEFAULT, listener: Consumer<T>) {
        this.handler.register(type, priority, phase, listener)
    }

    /**
     * This adds a [ListenerHandler] to the [GlobalEventHandler].
     *
     * This will call [ListenerHandler.getListenersFor] whenever
     * an [Event] is broadcasted and invoke the listeners.
     *
     * @param handler The [ListenerHandler] to add.
     */
    @JvmStatic
    public fun addHandler(handler: ListenerHandler) {
        this.handlers.add(handler)
    }

    /**
     * This removes a [ListenerHandler] to the [GlobalEventHandler].
     *
     * @param handler The [ListenerHandler] to remove.
     */
    @JvmStatic
    public fun removeHandler(handler: ListenerHandler) {
        this.handlers.remove(handler)
    }

    internal fun suppressNextEvent(type: Class<out Event>) {
        if (!this.suppressed.add(type)) {
            this.logger.warn("Adding suppressed event (type: {}) twice", type)
        }
    }

    private fun checkRecursive(type: Class<out Event>): Boolean {
        val count = this.stack.getInt(type)
        if (count >= MAX_RECURSIONS) {
            this.logger.warn(
                "Detected recursive event (type: {}), suppressing...\nStacktrace: \n{}",
                type.simpleName,
                Thread.currentThread().stackTrace.joinToString("\n")
            )
            return true
        }
        return false
    }

    private fun checkThread(event: Event, type: Class<out Event>): Boolean {
        val server = Arcade.getServerOrNull()
        if (server == null) {
            if (event !is SafeServerlessEvent) {
                this.logger.warn(
                    "Detected broadcasted event (type: {}), before server created, may be unsafe...",
                    type.simpleName
                )
            }
        } else if (!server.isSameThread) {
            if (this.stopping) {
                return true
            }
            if (server.isStopped) {
                this.stopping = true
                this.logger.warn(
                    "Event broadcasted (type: {}) while server is stopping, ignoring events...",
                    type.simpleName
                )
                return true
            }
            if (event !is ServerOffThreadEvent) {
                this.logger.warn(
                    "Detected broadcasted event (type: {}) off main thread, pushing to main thread...",
                    type.simpleName
                )
            }
            if (event is CancellableEvent) {
                event.offthread = true
            }
            server.execute { this.broadcast(event) }
            return true
        }
        return false
    }
}
