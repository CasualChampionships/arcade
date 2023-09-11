package net.casual.arcade.utils.json

import com.google.gson.JsonElement

interface JsonSerializer<T> {
    fun serialize(value: T): JsonElement

    fun deserialize(json: JsonElement): T
}