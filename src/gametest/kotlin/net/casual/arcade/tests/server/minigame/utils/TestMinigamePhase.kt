/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import com.mojang.serialization.Codec
import net.casual.arcade.minigame.phase.MinigamePhase
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs

enum class TestMinigamePhase: MinigamePhase {
    Grace,
    Active,
    Over;

    companion object {
        val CODEC: Codec<TestMinigamePhase> = ArcadeExtraCodecs.enum(TestMinigamePhase::id)
    }
}
