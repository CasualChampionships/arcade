/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events

import it.unimi.dsi.fastutil.objects.Reference2IntMap
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import kotlinx.atomicfu.atomic
import net.casual.arcade.events.common.ClientSideEvent
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.common.MissingExecutorEvent
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.phase.EventPhases
import net.casual.arcade.events.threading.ThreadingStrategy
import net.casual.arcade.utils.collection.mergeSorted
import net.casual.arcade.utils.server.ServerSingleton
import net.minecraft.client.Minecraft
import net.minecraft.server.MinecraftServer
import net.minecraft.util.thread.ReentrantBlockableEventLoop
import org.slf4j.LoggerFactory
import java.util.concurrent.CopyOnWriteArraySet
import java.util.concurrent.Executor

/**
 * Object class that is responsible for broadcasting
 * events and announcing events to registered listeners.
 *
 * @see broadcast
 * @see addProvider
 * @see Event
 */
public sealed class GlobalEventHandler<E: Event>(
    private val name: String,
    private val type: Class<E>
): ListenerRegistry<E> by SimpleListenerRegistry(type) {
    private val stack = ThreadLocal.withInitial { Reference2IntOpenHashMap<Class<out Event>>() }
    private val registries = CopyOnWriteArraySet<ListenerProvider>()

    private val injected = CopyOnWriteArraySet<InjectedListenerProvider<E>>()

    private val recursion = ScopedValue.newInstance<Unit>()

    private var stopping = atomic(false)

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
     * It is also possible to register to the firing event
     * as it's being broadcast.
     * These listeners will be deferred and will not
     * be invoked, the reasoning for this is because we
     * cannot guarantee priority preservation.
     *
     * @param E The type of event.
     * @param event The event that is being fired.
     * @param phases The phases of the event that should be invoked.
     */
    @JvmOverloads
    @JvmName("broadcast")
    public fun broadcast(event: E, phases: EventPhases = BuiltInEventPhases.DEFAULT_PHASES) {
        val type = event.javaClass

        @Suppress("UNCHECKED_CAST")
        val base = this.getListenersFor(type) as List<EventListener<E>>
        if (base.isEmpty() && this.registries.isEmpty() && this.injected.isEmpty()) {
            return
        }

        val executor = this.getMainThreadExecutor(event, type)
        if (executor == ThreadExecutor.Invalid) {
            return
        }

        val stack = this.stack.get()
        if (!this.recursion.isBound && this.checkRecursive(stack, type)) {
            return
        }

        // We could probably optimize this further by collecting all listeners
        // *then* merging them all in one go, we should also probably be filtering
        // by phase when merging listeners.
        val listeners = if (base.isEmpty()) ArrayList() else ArrayList(base)
        try {
            stack.addTo(type, 1)

            for (handler in this.registries) {
                @Suppress("UNCHECKED_CAST")
                listeners.mergeSorted(handler.getListenersFor(type) as List<EventListener<E>>)
            }
            for (injected in this.injected) {
                injected.injectListenerProviders(event) { handler ->
                    @Suppress("UNCHECKED_CAST")
                    listeners.mergeSorted(handler.getListenersFor(type) as List<EventListener<E>>)
                }
            }

            for (listener in listeners) {
                if (phases.contains(listener.phase)) {
                    val option = listener.strategy.get(event)
                    when (option) {
                        ThreadingStrategy.Option.UseCurrentThread -> listener.invoke(event)
                        ThreadingStrategy.Option.ForceMainThread -> executor.execute { listener.invoke(event) }
                    }
                }
            }
        } finally {
            stack.addTo(type, -1)
        }
    }

    /**
     * This adds a [ListenerProvider] to the [GlobalEventHandler].
     *
     * This will call [ListenerProvider.getListenersFor] whenever
     * an [Event] is broadcasted and invoke the listeners.
     *
     * @param handler The [ListenerProvider] to add.
     */
    public fun addProvider(handler: ListenerProvider) {
        this.registries.add(handler)
    }

    /**
     * This removes a [ListenerProvider] from the [GlobalEventHandler].
     *
     * @param handler The [ListenerProvider] to remove.
     */
    public fun removeProvider(handler: ListenerProvider) {
        this.registries.remove(handler)
    }

    /**
     * This adds [InjectedListenerProvider], which allows us to dynamically
     * add [ListenerProvider]s depending on the specific event being broadcasted.
     *
     * This may help performance instead.
     * Instead of each minigame registering for a specific player event,
     * we can instead add an injected listener provider which gets the
     * player's minigame then adds that minigame's listener provider.
     *
     * @param injected The [InjectedListenerProvider] to add.
     * @see InjectedListenerProvider
     */
    public fun addInjectedProvider(injected: InjectedListenerProvider<E>) {
        this.injected.add(injected)
    }

    /**
     * This removes an [InjectedListenerProvider] from the [GlobalEventHandler].
     *
     * @param injected The [InjectedListenerProvider] to remove.
     */
    public fun removeInjectedProvider(injected: InjectedListenerProvider<E>) {
        this.injected.remove(injected)
    }

    /**
     * This enables the recursion flag which allows you to have
     * recursive events.
     * This bypasses recursion safety implemented by this event handler.
     *
     * @param block The function to execute while recursion is allowed.
     */
    public fun recursive(block: () -> Unit) {
        ScopedValue.where(this.recursion, Unit).run(block)
    }

    protected abstract fun getExecutor(): ReentrantBlockableEventLoop<*>?

    private fun checkRecursive(stack: Reference2IntMap<Class<out Event>>, type: Class<out Event>): Boolean {
        val count = stack.getInt(type)
        if (count >= MAX_RECURSIONS) {
            logger.warn(
                "Detected recursive event (type: {}), suppressing...\nStacktrace: \n{}",
                type.simpleName,
                Thread.currentThread().stackTrace.joinToString("\n")
            )
            return true
        }
        return false
    }

    private fun getMainThreadExecutor(event: Event, type: Class<out Event>): ThreadExecutor {
        val executor = this.getExecutor()
        if (executor == null) {
            if (event !is MissingExecutorEvent) {
                logger.warn(
                    "Detected broadcasted event (type: {}), before {} was created, may be unsafe...",
                    type.simpleName,
                    this.name.lowercase()
                )
            }
            return ThreadExecutor.Current
        }
        if (executor.isSameThread) {
            return ThreadExecutor.Current
        }

        val isStopped = executor is MinecraftServer && executor.isStopped
        if (isStopped != this.stopping.value) {
            val wasStopping = this.stopping.getAndSet(isStopped)
            if (!wasStopping && isStopped) {
                logger.warn(
                    "Event broadcasted (type: {}) while {} is stopping, ignoring events...",
                    type.simpleName,
                    this.name.lowercase()
                )
            }
        }
        return if (isStopped) ThreadExecutor.Invalid else ThreadExecutor.Custom(executor)
    }

    private sealed class ThreadExecutor {
        object Invalid: ThreadExecutor()
        object Current: ThreadExecutor()
        class Custom(val executor: Executor): ThreadExecutor()

        inline fun execute(crossinline block: () -> Unit) {
            when (this) {
                Current -> block.invoke()
                is Custom -> this.executor.execute { block.invoke() }
                Invalid -> throw IllegalStateException()
            }
        }
    }

    private object ServerHandler: GlobalEventHandler<ServerSideEvent>("Server", ServerSideEvent::class.java) {
        override fun getExecutor(): ReentrantBlockableEventLoop<*>? {
            return ServerSingleton.getOrNull()
        }
    }

    private object ClientHandler: GlobalEventHandler<ClientSideEvent>("Client", ClientSideEvent::class.java) {
        override fun getExecutor(): ReentrantBlockableEventLoop<*> {
            return Minecraft.getInstance()
        }
    }

    public companion object {
        private const val MAX_RECURSIONS = 10

        private val logger = LoggerFactory.getLogger("ArcadeEventHandler")

        /**
         * The global broadcaster from the logical server-side.
         */
        @JvmField
        public val Server: GlobalEventHandler<ServerSideEvent> = ServerHandler

        /**
         * The global broadcaster from the logical client-side.
         */
        @JvmField
        public val Client: GlobalEventHandler<ClientSideEvent> = ClientHandler
    }
}
