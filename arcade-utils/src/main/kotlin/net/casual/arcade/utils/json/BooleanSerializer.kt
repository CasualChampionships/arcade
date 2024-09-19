package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public object BooleanSerializer: JsonSerializer<Boolean> {
    override fun serialize(value: Boolean): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Boolean {
        return json.asBoolean
    }

    override fun type(): String {
        return "boolean"
    }
}