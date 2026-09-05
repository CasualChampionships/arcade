/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.scope

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
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

public class MinigameScopes internal constructor(
    private val minigame: Minigame
) {
    private val scheduler = SimpleTickedScheduler.server()
    private val scopes = ReferenceLinkedOpenHashSet<MinigameScope>()

    internal val executing: Boolean
        get() = this.scheduler.ticking

    public val root: MinigameScope = this.create(MinigamePhaseLifetime.Forever)

    public fun create(lifetime: MinigamePhaseLifetime): MinigameScope {
        val scope = MinigameScope(this.minigame, lifetime, this)
        this.scopes.add(scope)
        return scope
    }

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
                scope.close()
            }
        }
    }

    internal fun cancelAll() {
        this.scheduler.cancelAll()
    }

    internal fun close() {
        for (scope in ArrayList(this.scopes)) {
            scope.close()
        }
        this.scheduler.cancelAll()
    }

    internal fun serialize(output: ValueOutput.ValueOutputList) {
        val lifetimes = Reference2ObjectOpenHashMap<ScheduledTask, MinigamePhaseLifetime>()
        for (scope in this.scopes) {
            if (scope.lifetime == MinigamePhaseLifetime.Forever) {
                continue
            }
            for (task in scope.scheduled()) {
                lifetimes[task] = scope.lifetime
            }
        }

        val codec = MinigamePhaseLifetime.codec(this.minigame.phases.codec)
        this.scheduler.serialize(output) { scheduled, data ->
            val lifetime = lifetimes[scheduled]
            if (lifetime != null) {
                data.store("lifetime", codec, lifetime)
            }
        }
    }

    internal fun deserialize(input: ValueInput.ValueInputList) {
        val codec = MinigamePhaseLifetime.codec(this.minigame.phases.codec)
        val restored = HashMap<MinigamePhaseLifetime, MinigameScope>()
        this.scheduler.deserialize(input, this.minigame) { scheduled, data ->
            val scope = when (val lifetime = data.read("lifetime", codec).orElse(MinigamePhaseLifetime.Forever)!!) {
                MinigamePhaseLifetime.Forever -> this.root
                else -> restored.getOrPut(lifetime) { this.create(lifetime) }
            }
            scope.track(scheduled)
        }
    }
}
