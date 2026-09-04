/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.component.impl.DefaultStatsTrackingComponent
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.minecraft.core.Registry

/**
 * This provides a way to create a given [MinigameComponent].
 *
 * The [MinigameComponentFactory] interface complements
 * [SerializableMinigameComponent] and allows the components
 * to be reconstructed after a minigame reload. See the
 * [SerializableMinigameComponent] documentation for
 * more information about how to properly serialize components.
 *
 * These factories should be registered in
 * [MinigameRegistries.MINIGAME_COMPONENT_FACTORY]
 * which can be done by calling [register].
 *
 * @see SerializableMinigameComponent
 */
public fun interface MinigameComponentFactory {
    /**
     * Creates an instance of a given [MinigameComponent].
     *
     * @param minigame The owner of the component.
     * @return The constructed component.
     */
    public fun create(minigame: Minigame): MinigameComponent

    public companion object {
        /**
         * Registers a given [MinigameComponentFactory] using the
         * constructed component's [MinigameComponent.type].
         *
         * @param type The type of the component that [factory] creates.
         * @param factory The factory that constructs the components.
         */
        public fun <C: MinigameComponent> register(type: MinigameComponentType<C>, factory: MinigameComponentFactory) {
            Registry.register(MinigameRegistries.MINIGAME_COMPONENT_FACTORY, type.id, factory)
        }

        internal fun bootstrap(registry: Registry<MinigameComponentFactory>) {
            Registry.register(registry, DefaultStatsTrackingComponent.TYPE.id, DefaultStatsTrackingComponent)
        }
    }
}
