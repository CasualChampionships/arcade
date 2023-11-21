package net.casual.arcade.stats

import com.google.gson.JsonElement

public class Stat<T>(
    public val stat: StatType<T>
) {
    public var value: T = this.stat.default

    public fun serialize(): JsonElement {
        return this.stat.serializer.serialize(this.value)
    }

    public fun deserialize(element: JsonElement) {
        this.value = this.stat.serializer.deserialize(element)
    }
}