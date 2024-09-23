package net.casual.arcade.utils.json

import com.google.gson.JsonElement

public interface JsonSerializer<T> {
    public fun serialize(value: T): JsonElement

    public fun deserialize(json: JsonElement): T

    public fun type(): String {
        return "unknown"
    }
}