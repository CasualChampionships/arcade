/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.managers

import com.mojang.serialization.Codec
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.MinigameState
import net.casual.arcade.minigame.events.MinigameSetPhaseEvent
import net.casual.arcade.minigame.managers.phase.AdvancingPhaseRoutine
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.minigame.phase.MinigamePhaseLifetime
import net.casual.arcade.minigame.managers.phase.MinigamePhaseRoutines
import net.casual.arcade.utils.time.MinecraftTimeDuration
import kotlin.enums.EnumEntries

public class MinigamePhaseManager internal constructor(
    private val minigame: Minigame,
    declared: EnumEntries<*>
): Iterable<MinigamePhase> {
    private val phases: List<MinigamePhase> = this.validate(declared)
    private var pending: MinigamePhase? = null

    public val routines: MinigamePhaseRoutines = MinigamePhaseRoutines(this, this.minigame)

    public val codec: Codec<MinigamePhase> = Codec.stringResolver(MinigamePhase::id, this::get)

    public fun get(id: String): MinigamePhase? {
        return this.phases.find { it.id == id }
    }

    public operator fun contains(phase: MinigamePhase): Boolean {
        return this.phases.contains(phase)
    }

    public fun set(phase: MinigamePhase, force: Boolean = false) {
        val state = this.minigame.state
        if (state !is MinigameState.Playing) {
            throw IllegalStateException("Cannot set phase of minigame '${this.minigame.id}', it is not playing")
        }
        if (state.phase == phase && !force) {
            return
        }
        if (!this.contains(phase)) {
            throw IllegalArgumentException("Cannot set minigame '${this.minigame.id}' phase to ${phase.id}")
        }

        val previous = state.phase
        this.pending = null
        this.minigame.scopes.setPhase(previous, phase)
        this.minigame.state = MinigameState.Playing(phase)
        this.enter(phase, previous)
    }

    public fun all(): List<MinigamePhase> {
        return this.phases
    }

    override fun iterator(): Iterator<MinigamePhase> {
        return this.all().iterator()
    }

    internal fun first(): MinigamePhase {
        return this.phases.first()
    }

    internal fun enter(phase: MinigamePhase, previous: MinigamePhase?) {
        val routine = this.routines[phase]
        if (routine != null) {
            val scope = this.minigame.scopes.create(MinigamePhaseLifetime.Current)
            scope.schedule(MinecraftTimeDuration.ZERO, AdvancingPhaseRoutine(routine))
        }

        GlobalEventHandler.Server.broadcast(MinigameSetPhaseEvent(this.minigame, phase, previous))
    }

    internal fun restore(phase: MinigamePhase) {
        this.minigame.state = MinigameState.Playing(phase)
    }

    internal fun requestAdvance() {
        val current = this.minigame.phaseOrNull ?: return
        this.pending = this.phases.getOrNull(current.ordinal + 1)
    }

    internal fun tick() {
        val pending = this.pending ?: return
        this.pending = null
        this.set(pending)
    }

    private fun validate(declared: EnumEntries<*>): List<MinigamePhase> {
        val id = this.minigame.id
        if (declared.isEmpty()) {
            throw IllegalStateException("Minigame $id must declare at least one phase")
        }

        val ids = HashSet<String>()
        val phases = ArrayList<MinigamePhase>(declared.size)
        for (constant in declared) {
            check(constant is MinigamePhase) {
                "Phases ${constant.declaringJavaClass.simpleName} does not implement MinigamePhase"
            }
            if (!ids.add(constant.id)) {
                throw IllegalStateException("Minigame $id has multiple phases with the id '${constant.id}'")
            }
            phases.add(constant)
        }
        return phases
    }
}
