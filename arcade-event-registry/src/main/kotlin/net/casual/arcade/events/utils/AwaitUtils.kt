/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.utils

import kotlinx.coroutines.suspendCancellableCoroutine
import net.casual.arcade.events.EventListener
import net.casual.arcade.events.EventListenerHandle
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.common.ClientSideEvent
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.threading.ThreadingStrategy
import net.casual.arcade.events.threading.ThreadingTarget
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume

/**
 * Suspends until an event of the given [type] is broadcast which
 * matches the [predicate], then returns that event.
 *
 * The listener is only registered while suspended; it is removed
 * as soon as the event is received, or if the coroutine is cancelled.
 *
 * @param T The type of event to await.
 * @param type The class of the event to await.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param strategy The [ThreadingStrategy] that the listener will use.
 * @param predicate The predicate which determines whether an event resumes.
 * @return The event which resumed the coroutine.
 * @see ListenerRegistry.register
 */
public suspend fun <E: Event, T: E> ListenerRegistry<E>.await(
    type: Class<T>,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    strategy: ThreadingStrategy = ThreadingTarget.Default,
    predicate: (T) -> Boolean = { true }
): T {
    return suspendCancellableCoroutine { continuation ->
        val resumed = AtomicBoolean()
        val reference = AtomicReference<EventListenerHandle>()
        fun removeRegisteredListener() {
            reference.getAndSet(null)?.remove()
        }

        val handle = this.register(type, EventListener.of(priority, phase, strategy) { event ->
            if (predicate.invoke(event) && resumed.compareAndSet(false, true)) {
                removeRegisteredListener()
                continuation.resume(event)
            }
        })
        reference.set(handle)

        if (resumed.get()) {
            removeRegisteredListener()
        }
        continuation.invokeOnCancellation { removeRegisteredListener() }
    }
}

/**
 * Suspends until an event of type [T] is broadcast which matches
 * the [predicate], then returns that event.
 *
 * @param T The type of event to await.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param strategy The [ThreadingStrategy] that the listener will use.
 * @param predicate The predicate which determines whether an event resumes.
 * @return The event which resumed the coroutine.
 */
public suspend inline fun <reified T: E, reified E: Event> ListenerRegistry<E>.await(
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    strategy: ThreadingStrategy = ThreadingTarget.Default,
    noinline predicate: (T) -> Boolean = { true }
): T {
    return this.await(T::class.java, priority, phase, strategy, predicate)
}

/**
 * Suspends until an event of type [T] is broadcast which matches
 * the [predicate], then returns that event.
 *
 * @param T The type of event to await.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param strategy The [ThreadingStrategy] that the listener will use.
 * @param predicate The predicate which determines whether an event resumes.
 * @return The event which resumed the coroutine.
 */
@JvmName("awaitServer")
public suspend inline fun <reified T: ServerSideEvent> ListenerRegistry<ServerSideEvent>.await(
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    strategy: ThreadingStrategy = ThreadingTarget.Default,
    noinline predicate: (T) -> Boolean = { true }
): T {
    return this.await<T, ServerSideEvent>(priority, phase, strategy, predicate)
}

/**
 * Suspends until an event of type [T] is broadcast which matches
 * the [predicate], then returns that event.
 *
 * @param T The type of event to await.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param strategy The [ThreadingStrategy] that the listener will use.
 * @param predicate The predicate which determines whether an event resumes.
 * @return The event which resumed the coroutine.
 */
@JvmName("awaitClient")
public suspend inline fun <reified T: ClientSideEvent> ListenerRegistry<ClientSideEvent>.await(
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    strategy: ThreadingStrategy = ThreadingTarget.Default,
    noinline predicate: (T) -> Boolean = { true }
): T {
    return this.await<T, ClientSideEvent>(priority, phase, strategy, predicate)
}
