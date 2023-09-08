package net.casual.arcade.settings

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

class DoubleGameSetting(
    name: String,
    value: Double,
    options: Map<String, Double> = emptyMap()
): GameSetting<Double>(name, value, options) {
    override fun serialise(value: Double): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialise(json: JsonElement): Double {
        return json.asDouble
    }
}