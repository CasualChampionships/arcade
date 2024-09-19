package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public object StringSerializer: JsonSerializer<String> {
    override fun serialize(value: String): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): String {
        return json.asString
    }

    override fun type(): String {
        return "string"
    }
}