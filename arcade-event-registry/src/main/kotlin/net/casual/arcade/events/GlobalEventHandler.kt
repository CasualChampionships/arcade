/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import it.unimi.dsi.fastutil.objects.ObjectSets
import it.unimi.dsi.fastutil.objects.Reference2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
import net.casual.arcade.events.BuiltInEventPhases.DEFAULT
import net.casual.arcade.events.common.CancellableEvent
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.common.MissingExecutorEvent
import net.casual.arcade.utils.ServerUtils
import net.casual.arcade.utils.addSorted
import net.minecraft.client.Minecraft
import net.minecraft.util.thread.ReentrantBlockableEventLoop
import org.apache.logging.log4j.LogManager
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.concurrent.Executor
import java.util.function.Consumer

/**
 * Object class that is responsible for broadcasting
 * events and announcing events to registered listeners.
 *
 * @see broadcast
 * @see addProvider
 * @see Event
 */
public enum class GlobalEventHandler(
    private val executor: () -> ReentrantBlockableEventLoop<*>?
): ListenerRegistry by SimpleListenerRegistry() {
    Server(ServerUtils::getServerOrNull),
    Client({ Minecraft.getInstance() });

    private val stack = ThreadLocal.withInitial { Reference2IntOpenHashMap<Class<out Event>>() }
    private val registries = ObjectSets.synchronize(ObjectOpenHashSet<ListenerProvider>())

    private val injected = ObjectSets.synchronize(ObjectOpenHashSet<InjectedListenerProvider>())

    private var recursion = ThreadLocal.withInitial { false }

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
    @JvmOverloads
    public fun <T: Event> broadcast(event: T, phases: Set<String> = BuiltInEventPhases.DEFAULT_PHASES) {
        val type = event::class.java

        // If this returns null, then the server is stopping anyway
        val executor = this.getMainThreadExecutor(event, type) ?: return

        if (!this.recursion.get() && this.checkRecursive(type)) {
            return
        }

        @Suppress("UNCHECKED_CAST")
        val listeners = ArrayList(this.getListenersFor(type)) as MutableList<EventListener<T>>
        try {
            this.stack.get().addTo(type, 1)

            synchronized(this.registries) {
                for (handler in this.registries) {
                    @Suppress("UNCHECKED_CAST")
                    listeners.addSorted(handler.getListenersFor(type) as List<EventListener<T>>)
                }
            }
            synchronized(this.injected) {
                for (injected in this.injected) {
                    injected.injectListenerProviders(event) { handler ->
                        @Suppress("UNCHECKED_CAST")
                        listeners.addSorted(handler.getListenersFor(type) as List<EventListener<T>>)
                    }
                }
            }

            for (listener in listeners) {
                if (phases.contains(listener.phase)) {
                    if (listener.requiresMainThread) {
                        executor.execute { listener.invoke(event) }
                    } else {
                        listener.invoke(event)
                    }
                }
            }
        } finally {
            this.stack.get().addTo(type, -1)
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
    public fun addInjectedProvider(injected: InjectedListenerProvider) {
        this.injected.add(injected)
    }

    /**
     * This removes an [InjectedListenerProvider] from the [GlobalEventHandler].
     *
     * @param injected The [InjectedListenerProvider] to remove.
     */
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
    public fun recursive(block: () -> Unit) {
        val previous = this.recursion.get()
        try {
            this.recursion.set(true)
            block()
        } finally {
            this.recursion.set(previous)
        }
    }

    private fun checkRecursive(type: Class<out Event>): Boolean {
        val count = this.stack.get().getInt(type)
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

    private fun getMainThreadExecutor(event: Event, type: Class<out Event>): Executor? {
        val executor = this.executor.invoke()
        if (executor == null) {
            if (event !is MissingExecutorEvent) {
                logger.warn(
                    "Detected broadcasted event (type: {}), before {} was created, may be unsafe...",
                    type.simpleName,
                    this.name.lowercase()
                )
            }
            return Executor(Runnable::run)
        }
        if (executor.isSameThread) {
            return Executor(Runnable::run)
        }
        if (this.stopping) {
            return null
        }
        if (!executor.scheduleExecutables()) {
            this.stopping = true
            logger.warn(
                "Event broadcasted (type: {}) while {} is stopping, ignoring events...",
                type.simpleName,
                this.name.lowercase()
            )
            return null
        }
        return executor
    }

    public companion object {
        private const val MAX_RECURSIONS = 10

        private val logger = LogManager.getLogger("ArcadeEventHandler")
    }
}
