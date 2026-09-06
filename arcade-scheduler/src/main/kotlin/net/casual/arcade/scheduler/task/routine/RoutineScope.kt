/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.routine

import com.mojang.serialization.Codec
import kotlinx.coroutines.CancellationException
import net.casual.arcade.events.ListenerRegistry
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.phase.BuiltInEventPhases
import net.casual.arcade.utils.time.MinecraftTimeDuration
import kotlin.coroutines.RestrictsSuspension

/**
 * The receiver a [Routine] body runs against.
 *
 * Only the suspending functions declared in this interface are callable.
 * This behavior allows the routine to be serialized and thus resumed if needed.
 *
 * @param O The type of the [owner] this routine runs against.
 * @see Routine
 */
@RestrictsSuspension
public interface RoutineScope<out O> {
    /**
     * The owner this routine is running for.
     */
    public val owner: O

    /**
     * Suspends the routine for a given [duration].
     *
     * A routine can be serialized while suspended here, when deserialized
     * the routine will resume from this point with only the remaining
     * delay duration.
     *
     * [onDelay] is invoked every time the routine enters this suspension
     * point, with the duration remaining before it resumes; the full
     * [duration] when the routine first reaches it, and however much is
     * left when the routine is restored:
     * ```kotlin
     * delay(duration) { remaining -> timer.setRemainingDuration(remaining) }
     * ```
     *
     * @param duration The duration to suspend for.
     * @param onDelay The action to run with the remaining duration.
     */
    public suspend fun delay(
        duration: MinecraftTimeDuration,
        onDelay: (remaining: MinecraftTimeDuration) -> Unit = { }
    )

    /**
     * Runs [block] exactly once, recording that it has run.
     *
     * When a routine is restored its body is immediately re-executed
     * from the top to rebuild its state; steps that have already run
     * are skipped. Any side effect which is followed by a [delay] must
     * therefore be inside a step, otherwise it will run again.
     *
     * Conversely, anything the routine needs to exist for as long as it
     * is suspended, such as a bossbar it displays, should be created
     * *outside* a step so that it is recreated when the routine is
     * restored.
     *
     * The [id] is optional, and is only used to detect that the routine's
     * body has changed since it was saved.
     *
     * @param id An optional identifier for this step.
     * @param block The action to run.
     */
    public suspend fun step(id: String? = null, block: () -> Unit)

    /**
     * Runs [block] exactly once, recording its result using [codec].
     *
     * When the routine is replayed the recorded result is returned
     * instead of running [block] again.
     *
     * @param codec The codec used to record the result.
     * @param id An optional identifier for this step.
     * @param block The action to run.
     * @return The result of [block], or the previously recorded result.
     */
    public suspend fun <T> step(codec: Codec<T>, id: String? = null, block: () -> T): T

    /**
     * Suspends the routine until an event of the given [type] is
     * broadcast to [registry] which matches the [predicate], then runs
     * [block] with that event.
     *
     * A routine can be serialized while suspended here, when deserialized
     * the routine will continue to suspend until the event is received,
     * or until cancellation.
     *
     * Similarly to [step] the [block] is run only once, when the event
     * is received (and matches the [predicate]). When the routine is
     * replayed after deserialization this will be skipped.
     * ```kotlin
     * await(PlayerDeathEvent::class.java, GlobalEventHandler.Global) { (player) ->
     *     player.sendSystemMessage(...)
     * }
     * ```
     *
     * The [id] is optional, and is only used to detect that the routine's
     * body has changed since it was saved.
     *
     * @param T The type of event to await.
     * @param type The class of the event to await.
     * @param registry The registry to listen for the event on.
     * @param id An optional identifier for this suspension point.
     * @param priority The priority of the listener.
     * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
     * @param predicate The predicate which determines whether an event resumes the routine.
     * @param block The action to run with the event which resumed the routine.
     */
    public suspend fun <E: Event, T: E> await(
        type: Class<T>,
        registry: ListenerRegistry<E>,
        id: String? = null,
        priority: Int = 1_000,
        phase: Int = BuiltInEventPhases.DEFAULT,
        predicate: (T) -> Boolean = { true },
        block: (T) -> Unit = { }
    )

    /**
     * Suspends the routine until an event of the given [type] is
     * broadcast to [registry] which matches the [predicate], then runs
     * [block] with that event, recording its result using [codec].
     *
     * A routine can be serialized while suspended here, when deserialized
     * the routine will continue to suspend until the event is received,
     * or until cancellation. When replaying the routine if the event had
     * already been triggered then the serialized value will be returned.
     * ```kotlin
     * val victim = await(PlayerDeathEvent::class.java, GlobalEventHandler.Server, UUIDUtil.CODEC) { (player) ->
     *     player.uuid
     * }
     * ```
     *
     * @param T The type of event to await.
     * @param R The type of the recorded result.
     * @param type The class of the event to await.
     * @param registry The registry to listen for the event on.
     * @param codec The codec used to record the result.
     * @param id An optional identifier for this suspension point.
     * @param priority The priority of the listener.
     * @param phase The phase of the event, [BuiltInEventPhases.DEFAULT] by default.
     * @param predicate The predicate which determines whether an event resumes the routine.
     * @param block The action to run with the event which resumed the routine.
     * @return The result of [block], or the previously recorded result.
     */
    public suspend fun <E: Event, T: E, R: Any> await(
        type: Class<T>,
        registry: ListenerRegistry<E>,
        codec: Codec<R>,
        id: String? = null,
        priority: Int = 1_000,
        phase: Int = BuiltInEventPhases.DEFAULT,
        predicate: (T) -> Boolean = { true },
        block: (T) -> R
    ): R

    /**
     * Suspends until cancellation, in which case it will throw a [CancellationException].
     *
     * @see kotlinx.coroutines.awaitCancellation
     */
    public suspend fun awaitCancellation(): Nothing
}
