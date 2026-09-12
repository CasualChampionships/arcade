/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.serialization

import kotlinx.coroutines.Job
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.scheduler.task.routine.Routine
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import org.jetbrains.annotations.ApiStatus.OverrideOnly

/**
 * This interface should be used on a minigame implementation
 * to mark it as being serializable.
 *
 * This then provides additional methods to describe how
 * your minigame should be serialized/deserialized.
 *
 * Below is the same minigame example as in the documentation for
 * [Minigame], except rewritten to support serialization.
 * Notably the phases are no longer raw coroutines, and instead
 * are standalone [Routine]s. Additionally a [MinigameFactory]
 * is needed to create instances of the minigame:
 * ```
 * class GraceRoutine: MinigameRoutine<ExampleMinigame> {
 *     override fun codec(): MapCodec<out Routine<ExampleMinigame>> {
 *         return codec
 *     }
 *
 *     override suspend fun RoutineScope<ExampleMinigame>.run() {
 *         try {
 *             // Any methods that should "run once", i.e. not run if
 *             // the Routine is reloaded should be wrapped in a step
 *             step { minigame.settings.canPvp.set(false) }
 *             delay(5.Minutes)
 *             step { minigame.chat.broadcast(Component.literal("The grace period is over!")) }
 *         } finally {
 *             step { minigame.settings.canPvp.set(true) }
 *         }
 *     }
 *
 *     companion object: CodecProvider<GraceRoutine> {
 *         override val id: Identifier = Identifier("modid", "grace")
 *         override val codec: MapCodec<out GraceRoutine> = MapCodec.unit(::GraceRoutine)
 *     }
 * }
 *
 * class ActiveRoutine: MinigameRoutine<ExampleMinigame> {
 *     override fun codec(): MapCodec<out Routine<ExampleMinigame>> {
 *         return codec
 *     }
 *
 *     override suspend fun RoutineScope<ExampleMinigame>.run() {
 *         minigame.scopes.current.register<PlayerDeathEvent> { (player) ->
 *             player.sendSystemMessage(Component.literal("You died!"))
 *         }
 *         awaitCancellation()
 *     }
 *
 *     companion object: CodecProvider<ActiveRoutine> {
 *         override val id: Identifier = Identifier("modid", "active")
 *         override val codec: MapCodec<out ActiveRoutine> = MapCodec.unit(::ActiveRoutine)
 *     }
 * }
 *
 * object ExampleMinigameFactory: MinigameFactory {
 *     private val CODEC = MapCodec.unit(this)
 *
 *     override fun create(context: MinigameCreationContext): Minigame {
 *         return ExampleMinigame(context.server, context.uuid)
 *     }
 *
 *     override fun codec(): MapCodec<out MinigameFactory> {
 *         return CODEC
 *     }
 * }
 *
 * class ExampleMinigame(
 *     server: MinecraftServer,
 *     uuid: UUID
 * ): Minigame(server, uuid, ID, ExamplePhase.entries), SerializableMinigame {
 *     // ...
 *
 *     @Listener
 *     private fun onInitialize(event: MinigameInitializeEvent) {
 *         // ...
 *
 *         this.phases.routines[ExamplePhase.Grace] = GraceRoutine()
 *         this.phases.routines[ExamplePhase.Active] = ActiveRoutine()
 *     }
 *
 *     override fun factory(): MinigameFactory {
 *         return ExampleMinigameFactory
 *     }
 *
 *     // ...
 * }
 * ```
 *
 * All [MinigameFactory]s and [Routine]s are required to be registered
 * in their respective registries:
 * ```
 * GraceRoutine.register(TaskRegistries.ROUTINE)
 * ActiveRoutine.register(TaskRegistries.ROUTINE)
 *
 * Registry.register(MinigameRegistries.MINIGAME_FACTORY, ExampleMinigame.ID, ExampleMinigameFactory)
 * ```
 *
 * @see Minigame
 */
@OverrideOnly
public interface SerializableMinigame {
    /**
     * The serialization version that the minigame is
     * currently on.
     *
     * When deserializing the [serializationVersion] of the
     * old minigame is passed into [deserialize] to allow
     * you to fix up any data that has changed over versions.
     */
    public val serializationVersion: Int
        get() = 0

    /**
     * The factory that creates instances of `this`
     * minigame.
     *
     * @return The factory that creates instances of this minigame.
     * @see MinigameFactory
     */
    public fun factory(): MinigameFactory

    /**
     * Serializes this minigame.
     *
     * @param output The output to serialize to.
     */
    public fun serialize(output: ValueOutput) {

    }

    /**
     * Deserializes this minigame.
     *
     * @param input The input to deserialize from.
     * @param version The version that the serialized minigame was on.
     */
    public fun deserialize(input: ValueInput, version: Int) {

    }
}

/**
 * This saves the minigame to disk.
 *
 * This runs asynchronously in the background but
 * can be joined via the returned [Job].
 *
 * @param M The minigame instance - is required to be both
 *   an instance of [Minigame] *and* [SerializableMinigame].
 * @return The serialization job, which can be awaited/joined.
 */
public fun <M> M.save(): Job where M: Minigame, M: SerializableMinigame {
    return this.serializer.saveTo(this, this.getSavePath())
}
