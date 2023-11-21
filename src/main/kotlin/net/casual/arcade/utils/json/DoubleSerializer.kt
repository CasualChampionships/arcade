package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public object DoubleSerializer: JsonSerializer<Double> {
    override fun serialize(value: Double): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Double {
        return json.asDouble
    }

    override fun type(): String {
        return "double"
    }
}