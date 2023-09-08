package net.casual.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class BooleanGameSetting(
    name: String,
    value: Boolean,
    options: Map<String, Boolean> = emptyMap()
): GameSetting<Boolean>(name, value, options) {
    override fun serialise(value: Boolean): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialise(json: JsonElement): Boolean {
        return json.asBoolean
    }
}