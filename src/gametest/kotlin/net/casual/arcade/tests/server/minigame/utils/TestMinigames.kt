/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import net.casual.arcade.minigame.component.MinigameComponentFactory
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.scheduler.utils.TaskRegistries
import net.casual.arcade.utils.serialization.codec.CodecProvider.Companion.register
import net.minecraft.core.Registry

object TestMinigames {
    fun register() {
        Registry.register(MinigameRegistries.MINIGAME_FACTORY, TestMinigame.ID, TestMinigame.codec())

        MinigameComponentFactory.register(TestScoreComponent.TYPE, TestScoreComponent)

        TestGraceRoutine.register(TaskRegistries.ROUTINE)
        TestActiveRoutine.register(TaskRegistries.ROUTINE)
        TestRoundRoutine.register(TaskRegistries.ROUTINE)
        TestSettingRoutine.register(TaskRegistries.ROUTINE)
    }
}
