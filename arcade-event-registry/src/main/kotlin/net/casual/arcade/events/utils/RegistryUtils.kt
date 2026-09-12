/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.utils

import net.casual.arcade.events.EventListenerHandle
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.common.ClientSideEvent
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.events.threading.ThreadingStrategy
import net.casual.arcade.events.threading.ThreadingTarget
import java.util.function.Consumer

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
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param strategy The [ThreadingStrategy] that the listener will use.
 * @param listener The callback which will be invoked when the event is fired.
 * @return A handle which can unregister the listener.
 */
public inline fun <reified T: E, reified E: Event> ListenerRegistry<E>.register(
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    strategy: ThreadingStrategy = ThreadingTarget.Default,
    listener: Consumer<T>
): EventListenerHandle {
    return this.register(T::class.java, priority, phase, strategy, listener)
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
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param strategy The [ThreadingStrategy] that the listener will use.
 * @param listener The callback which will be invoked when the event is fired.
 * @return A handle which can unregister the listener.
 */
@JvmName("registerServer")
public inline fun <reified T: ServerSideEvent> ListenerRegistry<ServerSideEvent>.register(
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    strategy: ThreadingStrategy = ThreadingTarget.Default,
    listener: Consumer<T>
): EventListenerHandle {
    return this.register<T, ServerSideEvent>(priority, phase, strategy, listener)
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
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param strategy The [ThreadingStrategy] that the listener will use.
 * @param listener The callback which will be invoked when the event is fired.
 * @return A handle which can unregister the listener.
 */
@JvmName("registerClient")
public inline fun <reified T: ClientSideEvent> ListenerRegistry<ClientSideEvent>.register(
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    strategy: ThreadingStrategy = ThreadingTarget.Default,
    listener: Consumer<T>
): EventListenerHandle {
    return this.register<T, ClientSideEvent>(priority, phase, strategy, listener)
}

/**
 * Registers an event listener.
 *
 * This allows you to register a callback to a specific event type.
 * This callback will **only** fire when instances of the given type
 * are fired.
 *
 * @param T The type of event.
 * @param listener The callback which will be invoked when the event is fired.
 * @return A handle which can unregister the listener.
 */
public inline fun <reified T: E, reified E: Event> ListenerRegistry<E>.register(listener: Consumer<T>): EventListenerHandle {
    return this.register(T::class.java, 1_000, BuiltInEventPhases.DEFAULT, ThreadingTarget.Default, listener)
}

/**
 * Registers an event listener.
 *
 * This allows you to register a callback to a specific event type.
 * This callback will **only** fire when instances of the given type
 * are fired.
 *
 * @param T The type of event.
 * @param listener The callback which will be invoked when the event is fired.
 * @return A handle which can unregister the listener.
 */
@JvmName("registerServer")
public inline fun <reified T: ServerSideEvent> ListenerRegistry<ServerSideEvent>.register(listener: Consumer<T>): EventListenerHandle {
    return this.register<T, ServerSideEvent>(listener)
}

/**
 * Registers an event listener.
 *
 * This allows you to register a callback to a specific event type.
 * This callback will **only** fire when instances of the given type
 * are fired.
 *
 * @param T The type of event.
 * @param listener The callback which will be invoked when the event is fired.
 * @return A handle which can unregister the listener.
 */
@JvmName("registerClient")
public inline fun <reified T: ClientSideEvent> ListenerRegistry<ClientSideEvent>.register(listener: Consumer<T>): EventListenerHandle {
    return this.register<T, ClientSideEvent>(listener)
}
