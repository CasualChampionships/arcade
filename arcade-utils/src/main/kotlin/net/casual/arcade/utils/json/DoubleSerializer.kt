/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive

public object DoubleSerializer: JsonSerializer<Double> {
    override fun serialize(value: Double): JsonElement {
        return JsonPrimitive(value)
    }

    override fun deserialize(json: JsonElement): Double {
        return json.asDouble
    }

    override fun type(): String {
        return "double"
    }
}