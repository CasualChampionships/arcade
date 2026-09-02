/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.component

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.minecraft.core.Registry

public fun interface MinigameComponentFactory {
    public fun create(minigame: Minigame): MinigameComponent

    public companion object {
        public fun <C: MinigameComponent> register(
            type: MinigameComponentType<C>,
            factory: MinigameComponentFactory
        ) {
            Registry.register(MinigameRegistries.MINIGAME_COMPONENT_FACTORY, type.id, factory)
        }

        internal fun bootstrap(registry: Registry<MinigameComponentFactory>) {

        }
    }
}
