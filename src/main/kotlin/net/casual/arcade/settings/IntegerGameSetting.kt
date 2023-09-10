package net.casual.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class IntegerGameSetting(
    name: String,
    value: Int,
    options: Map<String, Int> = emptyMap()
): GameSetting<Int>(name, value, options) {
    override fun serialize(value: Int): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Int {
        return json.asInt
    }
}