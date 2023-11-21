package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public object FloatSerializer: JsonSerializer<Float> {
    override fun serialize(value: Float): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Float {
        return json.asFloat
    }

    override fun type(): String {
        return "float"
    }
}