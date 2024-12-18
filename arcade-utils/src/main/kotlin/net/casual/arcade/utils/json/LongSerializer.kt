/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
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