package net.casual.arcade.events

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.GlobalEventHandler.addProvider
import net.casual.arcade.events.GlobalEventHandler.broadcast
import net.casual.arcade.events.core.CancellableEvent
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.player.PlayerEvent
import net.casual.arcade.events.server.SafeServerlessEvent
import net.casual.arcade.events.server.ServerCreatedEvent
import net.casual.arcade.events.server.ServerOffThreadEvent
import net.casual.arcade.utils.ServerUtils
import net.casual.arcade.utils.addSorted
import org.apache.logging.log4j.LogManager
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.function.Consumer

/**
 * Object class that is responsible for broadcasting
 * events and announcing events to registered listeners.
 *
 * @see broadcast
 * @see addProvider
 * @see Event
 */
public object GlobalEventHandler {
    private const val MAX_RECURSIONS = 10

    private val logger = LogManager.getLogger("ArcadeEventHandler")

    private val suppressed = ReferenceOpenHashSet<Class<out Event>>()
    private val stack = Reference2IntOpenHashMap<Class<out Event>>()
    private val registries = ObjectOpenHashSet<ListenerProvider>()
    private val registry = SimpleListenerRegistry()

    private val injected = ObjectOpenHashSet<InjectedListenerProvider>()

    private var recursion = false

    private var stopping = false

    /**
     * This broadcasts an event for all listeners.
     *
     * It is possible that listeners may **mutate** the
     * firing event, the caller should then handle this.
     * See the implementation details of the firing event.
     *
     * In the unlikely case that an event is fired within
     * one of its listeners, it will recurse, however, there is
     * a hard-limit to the number of times a recursive event
     * can be fired.
     * After this limit is reached, the event will be suppressed.
     *
     * This method *may* be called off the main thread,
     * however it must be defined behaviour; the event you're
     * broadcasting must implement [ServerOffThreadEvent].
     * The event will then be pushed to the main thread.
     *
     * It is also possible to register to the firing event
     * as it's being broadcast.
     * These listeners will be deferred and will not
     * be invoked, the reasoning for this is because we
     * cannot guarantee priority preservation.
     *
     * @param T The type of event.
     * @param event The event that is being fired.
     * @param phases The phases of the event that should be invoked.
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

        if (!this.recursion && this.checkRecursive(type)) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        val listeners = ArrayList(this.registry.getListenersFor(type)) as MutableList<EventListener<T>>
        try {
            this.stack.addTo(type, 1)

            for (handler in this.registries) {
                @Suppress("UNCHECKED_CAST")
                listeners.addSorted(handler.getListenersFor(type) as List<EventListener<T>>)
            }
            for (injected in this.injected) {
                injected.injectListenerProviders(event) { handler ->
                    @Suppress("UNCHECKED_CAST")
                    listeners.addSorted(handler.getListenersFor(type) as List<EventListener<T>>)
                }
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
     * @param phase The phase of the event, [DEFAULT] by default.
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
     * @param phase The phase of the event, [DEFAULT] by default.
     * @param listener The callback which will be invoked when the event is fired.
     */
    @JvmStatic
    @JvmOverloads
    public fun <T: Event> register(type: Class<T>, priority: Int = 1_000, phase: String = DEFAULT, listener: Consumer<T>) {
        this.registry.register(type, priority, phase, listener)
    }

    /**
     * This adds a [ListenerProvider] to the [GlobalEventHandler].
     *
     * This will call [ListenerProvider.getListenersFor] whenever
     * an [Event] is broadcasted and invoke the listeners.
     *
     * @param handler The [ListenerProvider] to add.
     */
    @JvmStatic
    public fun addProvider(handler: ListenerProvider) {
        this.registries.add(handler)
    }

    /**
     * This removes a [ListenerProvider] from the [GlobalEventHandler].
     *
     * @param handler The [ListenerProvider] to remove.
     */
    @JvmStatic
    public fun removeProvider(handler: ListenerProvider) {
        this.registries.remove(handler)
    }

    /**
     * This adds [InjectedListenerProvider], which allows us to dynamically
     * add [ListenerProvider]s depending on the specific event being broadcasted.
     *
     * This may help performance instead, for example, instead of each minigame
     * registering for a specific [PlayerEvent] then checking whether the player
     * from that event belongs in a minigame we can instead add an injected
     * listener provider which gets the player's minigame then adds that minigame's
     * listener provider.
     *
     * @param injected The [InjectedListenerProvider] to add.
     * @see InjectedListenerProvider
     */
    @JvmStatic
    public fun addInjectedProvider(injected: InjectedListenerProvider) {
        this.injected.add(injected)
    }

    /**
     * This removes an [InjectedListenerProvider] from the [GlobalEventHandler].
     *
     * @param injected The [InjectedListenerProvider] to remove.
     */
    @JvmStatic
    public fun removeInjectedProvider(injected: InjectedListenerProvider) {
        this.injected.remove(injected)
    }

    /**
     * This enables the recursion flag which allows you to have
     * recursive events.
     * This bypasses recursion safety implemented by this event handler.
     *
     * @param block The function to execute while recursion is allowed.
     */
    @JvmStatic
    public fun recursive(block: () -> Unit) {
        val previous = this.recursion
        try {
            this.recursion = true
            block()
        } finally {
            this.recursion = previous
        }
    }

    @JvmStatic
    @Experimental
    public fun enableRecursiveEvents() {
        this.recursion = true
    }

    @JvmStatic
    @Experimental
    public fun disableRecursiveEvents() {
        this.recursion = true
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
        val server = ServerUtils.getServerOrNull()
        if (server == null || type == ServerCreatedEvent::class.java) {
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
