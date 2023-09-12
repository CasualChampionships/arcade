package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public class IntSerializer: JsonSerializer<Int> {
    override fun serialize(value: Int): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Int {
        return json.asInt
    }
}