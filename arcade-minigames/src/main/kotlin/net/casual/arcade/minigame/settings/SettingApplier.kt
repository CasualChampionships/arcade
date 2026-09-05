/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

public fun interface SettingApplier<T: Any> {
    public fun onApply(setting: GameSetting<T>, value: T)
}
