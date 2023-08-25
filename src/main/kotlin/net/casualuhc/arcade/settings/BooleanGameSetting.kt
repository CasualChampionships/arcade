package net.casualuhc.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class BooleanGameSetting(
    name: String,
    value: Boolean,
    options: Map<String, Boolean> = emptyMap()
): GameSetting<Boolean>(name, value, options) {
    override fun serialise(): JsonElement {
        return JsonPrimitive(this.get())
    }

    override fun deserialise(json: JsonElement) {
        this.setQuietly(json.asBoolean)
    }
}