package net.casualuhc.arcade.settings

fun interface SettingListener<T: Any> {
    fun onSet(setting: GameSetting<T>, value: T)
}