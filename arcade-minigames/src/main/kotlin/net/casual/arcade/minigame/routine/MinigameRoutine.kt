/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.routine

import com.mojang.serialization.Codec
import net.casual.arcade.events.common.ServerSideEvent
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigamePhaseManager
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.task.routine.RoutineScope

/**
 * A [Routine] which runs against a [Minigame].
 *
 * @param M The minigame type.
 * @see Routine
 */
public interface MinigameRoutine<M: Minigame>: Routine<M>

/**
 * The minigame this routine is running for.
 *
 * This is an alias for [RoutineScope.owner].
 */
public val <M: Minigame> RoutineScope<M>.minigame: M
    get() = this.owner

/**
 * Suspends the routine until an event of type [T] is broadcast for the
 * [minigame] which matches the [predicate], then runs [block] with that event.
 *
 * A routine can be serialized while suspended here, when deserialized
 * the routine will continue to suspend until the event is received,
 * or until cancellation.
 *
 * Similarly to [step] the [block] is run only once, when the event
 * is received (and matches the [predicate]). When the routine is
 * replayed after deserialization this will be skipped.
 * ```kotlin
 * await<PlayerDeathEvent> { event -> minigame.players.spectate(event.player) }
 * ```
 *
 * The event is filtered for the minigame, see [Minigame.events].
 *
 * @param T The type of event to await.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @see RoutineScope.await
 */
public suspend inline fun <reified T: ServerSideEvent> RoutineScope<Minigame>.await(
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> Unit = { }
) {
    this.await(T::class.java, this.minigame.events, id, priority, phase, predicate, block)
}

/**
 *  Suspends the routine until an event of type [T] is broadcast for the
 *  [minigame] which matches the [predicate], then runs [block] with that event,
 *  recording its result using [codec].
 *
 *  A routine can be serialized while suspended here, when deserialized
 *  the routine will continue to suspend until the event is received,
 *  or until cancellation. When replaying the routine if the event had
 *  already been triggered then the serialized value will be returned.
 * ```kotlin
 * val victim = await<PlayerDeathEvent, _>(UUIDUtil.CODEC) { event -> event.player.uuid }
 * ```
 *
 * The event is filtered for the minigame, see [Minigame.events].
 *
 * @param T The type of event to await.
 * @param R The type of the recorded result.
 * @param codec The codec used to record the result.
 * @param id An optional identifier for this suspension point.
 * @param priority The priority of the listener.
 * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
 * @param predicate The predicate which determines whether an event resumes the routine.
 * @param block The action to run with the event which resumed the routine.
 * @return The result of [block], or the previously recorded result.
 * @see RoutineScope.await
 */
public suspend inline fun <reified T: ServerSideEvent, R: Any> RoutineScope<Minigame>.await(
    codec: Codec<R>,
    id: String? = null,
    priority: Int = 1_000,
    phase: Int = BuiltInEventPhases.DEFAULT,
    noinline predicate: (T) -> Boolean = { true },
    noinline block: (T) -> R
): R {
    return this.await(T::class.java, this.minigame.events, codec, id, priority, phase, predicate, block)
}

/**
 * This requests that the minigame changes to the given [phase].
 *
 * This is the preferred method for changing phases within a
 * [Routine] as calling [MinigamePhaseManager.set] can cancel
 * routines, and internally will defer to requesting a phase
 * change instead to avoid bugs. The result of this is that
 * when requesting a phase change it will not happen immediately
 * and instead only happen later in the tick.
 *
 * @param phase The phase to swap to.
 */
public suspend fun RoutineScope<Minigame>.requestPhase(phase: MinigamePhase) {
    this.step("request_phase") { this.minigame.phases.request(phase) }
}
