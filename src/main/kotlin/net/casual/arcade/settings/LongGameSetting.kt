package net.casual.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class LongGameSetting(
    name: String,
    value: Long,
    options: Map<String, Long> = emptyMap()
): GameSetting<Long>(name, value, options) {
    override fun serialize(value: Long): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Long {
        return json.asLong
    }
}