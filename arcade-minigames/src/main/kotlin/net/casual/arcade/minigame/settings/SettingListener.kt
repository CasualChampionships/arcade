package net.casual.arcade.minigame.settings

public fun interface SettingListener<T: Any> {
    public fun onSet(setting: GameSetting<T>, previous: T, value: T)
}