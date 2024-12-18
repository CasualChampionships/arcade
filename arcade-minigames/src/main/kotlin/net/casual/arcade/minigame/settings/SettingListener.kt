/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.settings

public fun interface SettingListener<T: Any> {
    public fun onSet(setting: GameSetting<T>, previous: T, value: T)
}