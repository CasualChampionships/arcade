/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.utils

import com.mojang.serialization.Codec
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.common.ClientSideEvent
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.scheduler.TickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineJournal
import net.casual.arcade.scheduler.task.routine.RoutineScope
import net.casual.arcade.scheduler.task.routine.RoutineTask
import net.casual.arcade.utils.time.MinecraftTimeDuration
import org.jetbrains.annotations.ApiStatus.Internal

/**
 * Runs another [routine] as part of this one.
 *
 * @param routine The routine to run.
 */
public suspend fun <O> RoutineScope<O>.call(routine: Routine<O>) {
    with(routine) { run() }
}

/**
 * Suspends the routine until an event of the given [T] is
 * broadcast to [registry] which matches the [predicate], then runs
 * [block] with that event.
 *
 * @param T The type of event to await.
 * @param registry The registry to listen for the event on.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @see RoutineScope.await
 */
public suspend inline fun <reified T: E, reified E: Event> RoutineScope<*>.await(
    registry: ListenerRegistry<E>,
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> Unit = { }
) {
    this.await(T::class.java, registry, id, priority, phase, predicate, block)
}

/**
 * Suspends the routine until an event of the given [T] is
 * broadcast to [registry] which matches the [predicate], then runs
 * [block] with that event.
 *
 * @param T The type of event to await.
 * @param registry The registry to listen for the event on.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @see RoutineScope.await
 */
@JvmName("awaitServer")
public suspend inline fun <reified T: ServerSideEvent> RoutineScope<*>.await(
    registry: ListenerRegistry<ServerSideEvent>,
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> Unit = { }
) {
    this.await<T, ServerSideEvent>(registry, id, priority, phase, predicate, block)
}

/**
 * Suspends the routine until an event of the given [T] is
 * broadcast to [registry] which matches the [predicate], then runs
 * [block] with that event.
 *
 * @param T The type of event to await.
 * @param registry The registry to listen for the event on.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @see RoutineScope.await
 */
@JvmName("awaitClient")
public suspend inline fun <reified T: ClientSideEvent> RoutineScope<*>.await(
    registry: ListenerRegistry<ClientSideEvent>,
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> Unit = { }
) {
    this.await<T, ClientSideEvent>(registry, id, priority, phase, predicate, block)
}

/**
 * Suspends the routine until an event of the given [T] is
 * broadcast to [registry] which matches the [predicate], then runs
 * [block] with that event, recording its result using [codec].
 *
 * @param T The type of event to await.
 * @param R The type of the recorded result.
 * @param registry The registry to listen for the event on.
 * @param codec The codec used to record the result.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @return The result of [block], or the previously recorded result.
 * @see RoutineScope.await
 */
public suspend inline fun <reified T: E, reified E: Event, R: Any> RoutineScope<*>.await(
    registry: ListenerRegistry<E>,
    codec: Codec<R>,
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> R
): R {
    return this.await(T::class.java, registry, codec, id, priority, phase, predicate, block)
}

/**
 * Suspends the routine until an event of the given [T] is
 * broadcast to [registry] which matches the [predicate], then runs
 * [block] with that event, recording its result using [codec].
 *
 * @param T The type of event to await.
 * @param R The type of the recorded result.
 * @param registry The registry to listen for the event on.
 * @param codec The codec used to record the result.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @return The result of [block], or the previously recorded result.
 * @see RoutineScope.await
 */
@JvmName("awaitServer")
public suspend inline fun <reified T: ServerSideEvent, R: Any> RoutineScope<*>.await(
    registry: ListenerRegistry<ServerSideEvent>,
    codec: Codec<R>,
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> R
): R {
    return this.await<T, ServerSideEvent, R>(registry, codec, id, priority, phase, predicate, block)
}

/**
 * Suspends the routine until an event of the given [T] is
 * broadcast to [registry] which matches the [predicate], then runs
 * [block] with that event, recording its result using [codec].
 *
 * @param T The type of event to await.
 * @param R The type of the recorded result.
 * @param registry The registry to listen for the event on.
 * @param codec The codec used to record the result.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @return The result of [block], or the previously recorded result.
 * @see RoutineScope.await
 */
@JvmName("awaitClient")
public suspend inline fun <reified T: ClientSideEvent, R: Any> RoutineScope<*>.await(
    registry: ListenerRegistry<ClientSideEvent>,
    codec: Codec<R>,
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> R
): R {
    return this.await<T, ClientSideEvent, R>(registry, codec, id, priority, phase, predicate, block)
}

/**
 * Schedules a [routine] to start after a given [delay].
 *
 * The routine's body does not begin running until the delay elapses,
 * and it may then suspend itself further with [RoutineScope.delay].
 *
 * @param delay The duration to wait before starting the [routine].
 * @param routine The routine to schedule, which must be registered.
 * @param owner The owner the routine runs against.
 * @return A handle which can be used to cancel the routine.
 * @throws IllegalArgumentException If the routine's codec is not registered.
 */
public fun <O> TickedScheduler.schedule(
    delay: MinecraftTimeDuration,
    routine: Routine<O>,
    owner: O
): ScheduledTask {
    routine.throwIfNotRegistered()
    val task = RoutineTask(routine, owner, RoutineJournal.create())
    this.schedule(delay, task)
    return task
}

/**
 * Schedules a [routine] to start at the end of the current tick.
 *
 * @param routine The routine to schedule, which must be registered.
 * @param owner The owner the routine runs against.
 * @return A handle which can be used to cancel the routine.
 * @throws IllegalArgumentException If the routine's codec is not registered.
 */
public fun <O> TickedScheduler.schedule(routine: Routine<O>, owner: O): ScheduledTask {
    return this.schedule(MinecraftTimeDuration.ZERO, routine, owner)
}

@Internal
public fun Routine<*>.throwIfNotRegistered() {
    require(TaskRegistries.ROUTINE.getKey(this.codec()) != null) {
        "Routine ${this.javaClass.name} must be registered in the routine registry"
    }
}