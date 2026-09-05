/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.component.MinigameComponentFactory
import net.casual.arcade.minigame.component.MinigameComponentType
import net.casual.arcade.minigame.component.SerializableMinigameComponent
import net.casual.arcade.minigame.scope.MinigameScope
import net.casual.arcade.utils.arcade
import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

class TestScoreComponent: SerializableMinigameComponent {
    var hits: Int = 0

    var initialized: Boolean = false
        private set

    var closed: Boolean = false
        private set

    override fun initialize(scope: MinigameScope) {
        this.initialized = true
    }

    override fun close() {
        this.closed = true
    }

    override fun serialize(output: ValueOutput) {
        output.putInt("hits", this.hits)
    }

    override fun deserialize(input: ValueInput, version: Int) {
        this.hits = input.getIntOr("hits", 0)
    }

    override fun type(): MinigameComponentType<*> {
        return TYPE
    }

    companion object: MinigameComponentFactory {
        val TYPE: MinigameComponentType<TestScoreComponent> = MinigameComponentType(arcade("test_score"))

        override fun create(minigame: Minigame): TestScoreComponent {
            return TestScoreComponent()
        }
    }
}
