/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.tests.server.minigame.utils

import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.settings.GameSetting
import net.casual.arcade.minigame.settings.GameSettingBuilder.Companion.bool
import net.casual.arcade.minigame.settings.MinigameSettings
import net.casual.arcade.minigame.utils.defaultOptions

class TestMinigameSettings(minigame: Minigame): MinigameSettings(minigame) {
    val changes = ArrayList<Pair<Boolean, Boolean>>()
    val observed = ArrayList<Boolean>()
    val applied = ArrayList<Boolean>()

    val testToggle: GameSetting<Boolean> = this.register(bool {
        name = "test_toggle"
        value = false
        defaultOptions()
        onChange { setting, previous, value ->
            changes.add(previous to value)
            observed.add(setting.get())
        }
        onApply { _, value ->
            applied.add(value)
        }
    })

    val testUndisplayed: GameSetting<Boolean> = this.register(bool {
        name = "test_undisplayed"
        value = false
        defaultOptions()
    })

    fun clearRecords() {
        this.changes.clear()
        this.observed.clear()
        this.applied.clear()
    }
}
