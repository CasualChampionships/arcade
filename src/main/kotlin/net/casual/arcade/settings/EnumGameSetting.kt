package net.casual.arcade.settings

import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.json.EnumSerializer

class EnumGameSetting<E: Enum<E>>(
    name: String,
    value: E,
    options: Map<String, E>,
): GameSetting<E>(name, value, options, EnumSerializer.of(options)) {
    companion object {
        fun <E: Enum<E>> of(name: String, value: E, type: Class<E>): EnumGameSetting<E> {
            return EnumGameSetting(name, value, EnumUtils.enumToMap(type))
        }
    }
}