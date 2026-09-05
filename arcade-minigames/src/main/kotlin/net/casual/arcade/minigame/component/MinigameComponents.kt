/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.annotation.MinigameEventListener
import net.casual.arcade.minigame.phase.MinigamePhaseLifetime
import net.casual.arcade.minigame.scope.MinigameScope
import net.minecraft.resources.Identifier

/**
 * The manager for [MinigameComponent]s.
 *
 * This handles adding, initializing, and removing components
 * for a specific [minigame] instance.
 *
 * @see Minigame.components
 */
public class MinigameComponents(private val minigame: Minigame) {
    private val components = LinkedHashMap<MinigameComponentType<*>, Attached>()

    /**
     * Adds the specified [component].
     *
     * The component will be initialized if the owning minigame
     * has been initialized, otherwise it will delegate this
     * until the minigame has been initialized.
     *
     * @throws IllegalStateException If the minigame is closed or if the
     *   minigame is in the process of deserializing.
     * @throws IllegalArgumentException If the specified [component] type
     *   already exists.
     */
    public fun add(component: MinigameComponent) {
        val type = component.type()
        check(!this.minigame.closed) { "Cannot add component to closed minigame ${this.minigame.id}" }
        check(!this.minigame.serializer.loading) { "Cannot add component to minigame ${this.minigame} while it's deserializing" }
        require(!this.components.containsKey(type)) { "Minigame ${this.minigame.id} already has component $type" }

        val attached = Attached(component)
        this.components[type] = attached

        if (this.minigame.initialized) {
            attached.initialize(this.minigame)
        }
    }

    /**
     * Removes the specified [component].
     *
     * The component will only be removed if the exact
     * component is currently added.
     * To remove a component by type call [remove]
     * by [MinigameComponentType] instead.
     *
     * @param component The component to remove.
     * @return Whether the component was successfully removed.
     */
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

    /**
     * Removes a component tied to the specified [type].
     *
     * @param type The type of the component to remove.
     * @return Whether the remove was successful.
     */
    public fun remove(type: MinigameComponentType<*>): Boolean {
        val attached = this.components.remove(type) ?: return false
        attached.close()
        return true
    }

    /**
     * This gets a [MinigameComponent] by providing its [MinigameComponentType].
     * Will return `null` if the component doesn't exist.
     *
     * @param type The type of the component to get.
     * @return The stored component, may be `null` if not added.
     */
    @Suppress("UNCHECKED_CAST")
    public fun <C: MinigameComponent> get(type: MinigameComponentType<C>): C? {
        return this.components[type]?.component as C?
    }

    /**
     * This gets a [MinigameComponent] by providing its [MinigameComponentType].
     * Will throw if the component doesn't exist.
     *
     * @param type The type of the component to get.
     * @return The stored component.
     * @throws IllegalArgumentException If the component is missing.
     */
    public fun <C: MinigameComponent> require(type: MinigameComponentType<C>): C {
        return requireNotNull(this.get(type)) { "Minigame ${this.minigame.id} does not have component $type" }
    }

    /**
     * Checks whether a component with the given [type] exists.
     *
     * @param type The type to check.
     * @return Whether a component with that type exists.
     */
    public fun has(type: MinigameComponentType<*>): Boolean {
        return this.components.containsKey(type)
    }

    /**
     * Gets all the components that have been added.
     *
     * @return All the [MinigameComponent]s.
     */
    public fun all(): Collection<MinigameComponent> {
        return this.components.values.map(Attached::component)
    }

    internal fun has(id: Identifier): Boolean {
        return this.components.keys.any { it.id == id }
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

    private class Attached(val component: MinigameComponent) {
        private var scope: MinigameScope? = null

        fun initialize(minigame: Minigame) {
            if (this.scope != null) {
                return
            }
            val scope = minigame.scopes.create(MinigamePhaseLifetime.Forever)
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
