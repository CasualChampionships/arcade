/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
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