package net.casual.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class LongGameSetting(
    name: String,
    value: Long,
    options: Map<String, Long> = emptyMap()
): GameSetting<Long>(name, value, options) {
    override fun serialise(): JsonElement {
        return JsonPrimitive(this.get())
    }

    override fun deserialise(json: JsonElement) {
        this.setQuietly(json.asLong)
    }
}