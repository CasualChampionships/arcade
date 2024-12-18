/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.time.MinecraftTimeDuration

public object TimeDurationSerializer: JsonSerializer<MinecraftTimeDuration> {
    override fun serialize(value: MinecraftTimeDuration): JsonElement {
        return JsonPrimitive(value.ticks)
    }

    override fun deserialize(json: JsonElement): MinecraftTimeDuration {
        return json.asInt.Ticks
    }

    override fun type(): String {
        return "ticks"
    }
}