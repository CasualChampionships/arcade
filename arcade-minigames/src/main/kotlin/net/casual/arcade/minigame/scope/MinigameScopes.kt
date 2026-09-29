/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.scope

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ReferenceLinkedOpenHashSet
import kotlinx.coroutines.CoroutineScope
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.minigame.phase.MinigamePhaseLifetime
import net.casual.arcade.scheduler.SimpleTickedScheduler
import net.casual.arcade.scheduler.task.ScheduledTask
import net.casual.arcade.scheduler.task.Task
import net.casual.arcade.scheduler.task.routine.Routine
import net.casual.arcade.scheduler.utils.schedule
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrDefault
import kotlin.jvm.optionals.getOrNull

/**
 * The manager for a [Minigame]'s [MinigameScope]s.
 *
 * @see MinigameScope
 * @see Minigame.scopes
 */
public class MinigameScopes internal constructor(
    private val minigame: Minigame
) {
    private val scheduler = SimpleTickedScheduler.server()
    private val scopes = ReferenceLinkedOpenHashSet<MinigameScope>()
    private val named = Object2ObjectOpenHashMap<Identifier, MinigameScope>()

    internal val executing: Boolean
        get() = this.scheduler.ticking

    /**
     * The default "root" scope which lives for the
     * minigame's entire lifetime.
     *
     * This scope cannot be closed manually.
     */
    public val root: MinigameScope = this.create(null, MinigamePhaseLifetime.Forever, closeable = false)

    /**
     * The "current" scope which always belongs to the minigame's
     * current phase; everything owned by it is cancelled or
     * unregistered whenever the phase changes.
     *
     * This scope cannot be closed manually.
     */
    public val current: MinigameScope = this.create(null, MinigamePhaseLifetime.Current, closeable = false)

    /**
     * Creates a new scope with a given [lifetime].
     *
     * @param lifetime The lifetime determining when the scope closes.
     * @return The created scope.
     * @see MinigamePhaseLifetime
     */
    public fun create(lifetime: MinigamePhaseLifetime): MinigameScope {
        return this.create(null, lifetime, true)
    }

    /**
     * Gets the scope with the given [id], creating it with
     * the given [lifetime] if it doesn't exist yet.
     *
     * Named scopes are never closed, when their [lifetime]
     * ends everything they own is cancelled and the scope
     * remains usable. Anything scheduled on a named scope
     * is restored into the same named scope when the
     * minigame is deserialized.
     *
     * @param id The unique id of the scope.
     * @param lifetime The lifetime determining when the scope is cancelled.
     * @return The named scope.
     * @throws IllegalArgumentException If a scope with the [id] already
     *   exists with a different [lifetime].
     */
    public fun named(id: Identifier, lifetime: MinigamePhaseLifetime): MinigameScope {
        val existing = this.named[id]
        if (existing != null) {
            require(existing.lifetime == lifetime) {
                "Scope $id of minigame ${this.minigame.id} already exists with lifetime ${existing.lifetime}"
            }
            return existing
        }
        val scope = this.create(id, lifetime, false)
        this.named[id] = scope
        return scope
    }

    /**
     * Gets all the scopes which are currently open.
     *
     * @return All the open scopes.
     */
    public fun all(): Collection<MinigameScope> {
        return this.scopes
    }

    internal fun coroutineScope(): CoroutineScope {
        return this.scheduler.asCoroutineScope()
    }

    internal fun schedule(delay: MinecraftTimeDuration, task: Task): ScheduledTask {
        return this.scheduler.schedule(delay, task)
    }

    @Suppress("UNCHECKED_CAST")
    internal fun <M: Minigame> schedule(delay: MinecraftTimeDuration, routine: Routine<M>): ScheduledTask {
        return this.scheduler.schedule(delay, routine, this.minigame as M)
    }

    private fun create(id: Identifier?, lifetime: MinigamePhaseLifetime, closeable: Boolean): MinigameScope {
        val scope = MinigameScope(this.minigame, lifetime, id, this, closeable)
        this.scopes.add(scope)
        return scope
    }

    internal fun remove(scope: MinigameScope) {
        this.scopes.remove(scope)
    }

    internal fun tick() {
        this.scheduler.tick()
        for (scope in this.scopes) {
            scope.prune()
        }
    }

    internal fun setPhase(previous: MinigamePhase, next: MinigamePhase) {
        for (scope in ArrayList(this.scopes)) {
            if (!scope.lifetime.survives(previous, next)) {
                scope.expire()
            }
        }
    }

    internal fun cancelAll() {
        this.scheduler.cancelAll()
    }

    internal fun close() {
        for (scope in ArrayList(this.scopes)) {
            scope.destroy()
        }
        this.named.clear()
        this.scheduler.cancelAll()
    }

    internal fun serialize(output: ValueOutput.ValueOutputList) {
        val owners = Reference2ObjectOpenHashMap<ScheduledTask, MinigameScope>()
        for (scope in this.scopes) {
            if (scope.id == null && scope.lifetime == MinigamePhaseLifetime.Forever) {
                continue
            }
            for (task in scope.scheduled()) {
                owners[task] = scope
            }
        }

        val codec = MinigamePhaseLifetime.codec(this.minigame.phases.codec)
        this.scheduler.serialize(output) { scheduled, data ->
            val scope = owners[scheduled]
            if (scope != null) {
                data.store("lifetime", codec, scope.lifetime)
                data.storeNullable("scope", Identifier.CODEC, scope.id)
            }
        }
    }

    private fun restoreNamed(id: Identifier, lifetime: MinigamePhaseLifetime): MinigameScope {
        val existing = this.named[id]
        if (existing != null && existing.lifetime != lifetime) {
            ArcadeUtils.logger.warn(
                "Scope $id of minigame ${this.minigame.id} was saved with lifetime $lifetime but now has ${existing.lifetime}"
            )
            return existing
        }
        return this.named(id, lifetime)
    }

    internal fun deserialize(input: ValueInput.ValueInputList) {
        val codec = MinigamePhaseLifetime.codec(this.minigame.phases.codec)
        val restored = HashMap<MinigamePhaseLifetime, MinigameScope>()
        this.scheduler.deserialize(input, this.minigame) { scheduled, data ->
            val lifetime = data.read("lifetime", codec).getOrDefault(MinigamePhaseLifetime.Forever)
            val id = data.read("scope", Identifier.CODEC).getOrNull()
            val scope = when {
                id != null -> this.restoreNamed(id, lifetime)
                lifetime == MinigamePhaseLifetime.Forever -> this.root
                lifetime == MinigamePhaseLifetime.Current -> this.current
                else -> restored.getOrPut(lifetime) { this.create(lifetime) }
            }
            scope.track(scheduled)
        }
    }
}
