/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.scheduler.task.routine

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.scheduler.utils.TaskRegistries
import net.minecraft.core.Registry
import java.util.function.Function

/**
 * A resumable unit of scheduled work.
 *
 * Unlike a [net.casual.arcade.scheduler.task.Task], which is transient,
 * a routine is designed to be serializable. Its own fields are
 * its state, serialized by the [MapCodec] it provides, and it may suspend
 * itself with [RoutineScope.delay].
 *
 * A routine's codec must be registered in [TaskRegistries.ROUTINE] before
 * it can be scheduled.
 * This should be implemented using a companion object helper:
 * ```kotlin
 * public class PokeRoutine(private val times: Int): Routine<Unit> {
 *     override fun codec(): MapCodec<out Routine<Unit>> {
 *         return codec
 *     }
 *
 *     override suspend fun RoutineScope<Unit>.run() {
 *         repeat(times) {
 *             step { println("Poke") }
 *             delay(20.Ticks)
 *         }
 *     }
 *
 *     public companion object: CodecProvider<PokeRoutine> {
 *         override val id: Identifier = Identifier.parse("example:poke")
 *
 *         override val codec: MapCodec<out PokeRoutine> = Codec.INT.fieldOf("times")
 *             .xmap(::PokeRoutine, PokeRoutine::times)
 *     }
 * }
 * ```
 *
 * @param O The type of the owner this routine runs against.
 * @see RoutineScope
 */
public interface Routine<O> {
    /**
     * The version of this routine's body.
     *
     * A routine saved under a different version cannot safely be replayed,
     * as its body may no longer issue the same sequence of suspension
     * points. Bump this whenever you change the structure of [run].
     */
    public val version: Int
        get() = 1

    /**
     * The codec used to serialize this routine, which must be registered
     * in [TaskRegistries.ROUTINE].
     */
    public fun codec(): MapCodec<out Routine<O>>

    /**
     * The body of the routine.
     */
    public suspend fun RoutineScope<O>.run()

    public companion object {
        public val CODEC: Codec<Routine<*>> = Codec.lazyInitialized {
            TaskRegistries.ROUTINE.byNameCodec().dispatch({ it.codec() }, Function.identity())
        }

        internal fun bootstrap(registry: Registry<MapCodec<out Routine<*>>>) {

        }
    }
}
