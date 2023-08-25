package net.casual.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.casual.arcade.utils.EnumUtils

class EnumGameSetting<E: Enum<E>>(
    name: String,
    value: E,
    options: Map<String, E>,
): GameSetting<E>(name, value, options) {
    override fun serialise(): JsonElement {
        return JsonPrimitive(this.get().name)
    }

    override fun deserialise(json: JsonElement) {
        this.setQuietly(this.getOption(json.asString)!!)
    }

    companion object {
        fun <E: Enum<E>> of(name: String, value: E, type: Class<E>): EnumGameSetting<E> {
            return EnumGameSetting(name, value, EnumUtils.enumToMap(type))
        }
    }
}