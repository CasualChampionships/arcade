/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.gametest.minigame

import net.casual.arcade.gametest.TestContext
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.component.MinigameComponent
import net.casual.arcade.minigame.phase.MinigamePhase
import net.minecraft.server.MinecraftServer
import java.util.UUID

public class TestMinigameBuilder<M: Minigame> internal constructor(
    private val context: TestContext,
    private val constructor: (MinecraftServer, UUID) -> M
) {
    private val components = ArrayList<MinigameComponent>()
    private val configurations = ArrayList<(M) -> Unit>()

    private var uuid: UUID = UUID.randomUUID()
    private var phase: MinigamePhase? = null
    private var removePhaseRoutines: Boolean = false

    public fun uuid(uuid: UUID): TestMinigameBuilder<M> {
        this.uuid = uuid
        return this
    }

    public fun phase(phase: MinigamePhase): TestMinigameBuilder<M> {
        this.phase = phase
        return this
    }

    public fun component(component: MinigameComponent): TestMinigameBuilder<M> {
        this.components.add(component)
        return this
    }

    public fun withoutPhaseRoutines(): TestMinigameBuilder<M> {
        this.removePhaseRoutines = true
        return this
    }

    public fun configure(block: (M) -> Unit): TestMinigameBuilder<M> {
        this.configurations.add(block)
        return this
    }

    public fun create(): M {
        val minigame = this.build()
        minigame.tryInitialize()
        this.tryRemovePhaseRoutines(minigame)
        return minigame
    }

    public fun start(): M {
        val minigame = this.build()
        minigame.start()
        this.tryRemovePhaseRoutines(minigame)

        val phase = this.phase
        if (phase != null) {
            minigame.phases.set(phase)
        }
        return minigame
    }

    private fun build(): M {
        val minigame = this.constructor.invoke(this.context.server, this.uuid)
        for (component in this.components) {
            minigame.components.add(component)
        }
        for (configuration in this.configurations) {
            configuration.invoke(minigame)
        }
        this.context.track(AutoCloseable(minigame::close))
        return minigame
    }

    private fun tryRemovePhaseRoutines(minigame: Minigame) {
        if (this.removePhaseRoutines) {
            for (phase in minigame.phases) {
                minigame.phases.routines.remove(phase)
            }
        }
    }
}
