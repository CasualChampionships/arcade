package net.casual.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.casual.arcade.utils.EnumUtils

class EnumGameSetting<E: Enum<E>>(
    name: String,
    value: E,
    options: Map<String, E>,
): GameSetting<E>(name, value, options) {
    override fun serialize(value: E): JsonElement {
        return JsonPrimitive(value.name)
    }

    override fun deserialize(json: JsonElement): E {
        return this.getOption(json.asString)!!
    }

    companion object {
        fun <E: Enum<E>> of(name: String, value: E, type: Class<E>): EnumGameSetting<E> {
            return EnumGameSetting(name, value, EnumUtils.enumToMap(type))
        }
    }
}