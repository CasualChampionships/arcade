package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public object LongSerializer: JsonSerializer<Long> {
    override fun serialize(value: Long): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Long {
        return json.asLong
    }

    override fun type(): String {
        return "long"
    }
}