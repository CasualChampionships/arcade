package net.casual.arcade.settings

public fun interface SettingListener<T: Any> {
    public fun onSet(setting: GameSetting<T>, previous: T, value: T)
}