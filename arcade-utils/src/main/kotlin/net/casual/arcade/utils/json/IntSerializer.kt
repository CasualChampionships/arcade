/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public object IntSerializer: JsonSerializer<Int> {
    override fun serialize(value: Int): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Int {
        return json.asInt
    }

    override fun type(): String {
        return "integer"
    }
}