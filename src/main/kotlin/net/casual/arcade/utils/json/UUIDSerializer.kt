package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import java.util.*

public object UUIDSerializer: JsonSerializer<UUID> {
    override fun serialize(value: UUID): JsonElement {
        return JsonPrimitive(value.toString())
    }

    override fun deserialize(json: JsonElement): UUID {
        return UUID.fromString(json.asString)
    }

    override fun type(): String {
        return "uuid"
    }
}