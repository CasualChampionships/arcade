/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.phase.PhaseLifetime
import net.casual.arcade.minigame.scope.MinigameScope
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput
import kotlin.jvm.optionals.getOrNull

public class MinigameComponents(private val minigame: Minigame) {
    private val components = LinkedHashMap<MinigameComponentType<*>, Attached>()

    public fun add(component: MinigameComponent) {
        val type = component.type()
        if (this.minigame.closed) {
            throw IllegalStateException("Cannot add component $type to closed minigame ${this.minigame.id}")
        }
        if (this.components.containsKey(type)) {
            throw IllegalArgumentException("Minigame ${this.minigame.id} already has component $type")
        }

        val attached = Attached(component)
        this.components[type] = attached

        if (this.minigame.initialized) {
            attached.initialize(this.minigame)
        }
    }

    public fun remove(component: MinigameComponent): Boolean {
        val type = component.type()
        val attached = this.components[type]
        if (attached == null || attached.component !== component) {
            return false
        }
        this.components.remove(type)
        attached.close()
        return true
    }

    public fun remove(type: MinigameComponentType<*>): Boolean {
        val attached = this.components.remove(type) ?: return false
        attached.close()
        return true
    }

    @Suppress("UNCHECKED_CAST")
    public fun <C: MinigameComponent> get(type: MinigameComponentType<C>): C? {
        return this.components[type]?.component as C?
    }

    public fun <C: MinigameComponent> require(type: MinigameComponentType<C>): C {
        return requireNotNull(this.get(type)) { "Minigame ${this.minigame.id} does not have component $type" }
    }

    public fun has(type: MinigameComponentType<*>): Boolean {
        return this.components.containsKey(type)
    }

    public fun all(): Collection<MinigameComponent> {
        return this.components.values.map(Attached::component)
    }

    internal fun initialize() {
        for (attached in ArrayList(this.components.values)) {
            attached.initialize(this.minigame)
        }
    }

    internal fun close() {
        for (attached in ArrayList(this.components.values)) {
            attached.close()
        }
        this.components.clear()
    }

    internal fun serialize(output: ValueOutput) {
        for ((type, attached) in this.components) {
            val component = attached.component
            if (component is SerializableComponent) {
                component.save(output.child(type.id.toString()))
            }
        }
    }

    internal fun deserialize(input: ValueInput) {
        for ((type, attached) in this.components) {
            val component = attached.component
            if (component is SerializableComponent) {
                val child = input.child(type.id.toString()).getOrNull() ?: continue
                component.load(child)
            }
        }
    }

    private class Attached(val component: MinigameComponent) {
        private var scope: MinigameScope? = null

        fun initialize(minigame: Minigame) {
            if (this.scope != null) {
                return
            }
            val scope = minigame.scopes.create(PhaseLifetime.Forever)
            this.scope = scope
            val component = this.component
            if (component is MinigameEventListener) {
                scope.addEventListener(component)
            }
            component.initialize(scope)
        }

        fun close() {
            this.scope?.close()
            this.scope = null
            this.component.close()
        }
    }
}
