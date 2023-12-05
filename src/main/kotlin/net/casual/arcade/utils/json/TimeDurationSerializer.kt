package net.casual.arcade.utils.json

import com.google.gson.JsonElement
import com.google.gson.JsonPrimitive
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.TimeUtils.Ticks

public object TimeDurationSerializer: JsonSerializer<MinecraftTimeDuration> {
    override fun serialize(value: MinecraftTimeDuration): JsonElement {
        return JsonPrimitive(value.toTicks())
    }

    override fun deserialize(json: JsonElement): MinecraftTimeDuration {
        return json.asInt.Ticks
    }
}