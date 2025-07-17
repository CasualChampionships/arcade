/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.casual.arcade.minigame.utils.MinigameRegistries
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.core.Holder
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

public class StatTracker {
    private val stats = ConcurrentHashMap<StatType<*>, Stat<*>>()
    private var frozen: Boolean = false

    public fun freeze() {
        this.frozen = true
        for (stat in this.stats.values) {
            stat.frozen = true
        }
    }

    public fun unfreeze() {
        this.frozen = false
        for (stat in this.stats.values) {
            stat.frozen = false
        }
    }

    public fun <T> getStatValueOrDefault(holder: Holder<StatType<T>>): T {
        val stat = this.stats[holder.value()] ?: return holder.value().default
        @Suppress("UNCHECKED_CAST")
        return (stat as Stat<T>).value
    }

    public fun <T> getOrCreateStat(holder: Holder<StatType<T>>): Stat<T> {
        @Suppress("UNCHECKED_CAST")
        return this.stats.getOrPut(holder.value()) {
            this.createStat(holder.value())
        } as Stat<T>
    }

    public fun serialize(): JsonArray {
        val stats = JsonArray()
        for ((type, stat) in this.stats) {
            val statData = JsonObject()
//            statData.addProperty("type", type.id.toString())
//            statData.add("value", stat.serialize())
//            statData.addProperty("value_type", stat.type.codec.type())
//            stats.add(statData)
        }
        return stats
    }

    public fun deserialize(stats: JsonArray) {
        for (statData in stats.objects()) {
//            val location = ResourceLocation.parse(statData.string("type"))
//            val value = statData["value"]
//            val type = statData.string("value_type")
//            this.unprocessed[location] = value to type
        }
    }

    private fun <T> codec(type: StatType<T>): Codec<Map<StatType<*>, T>> {
        return ArcadeExtraCodecs.keyedUnboundedMapCodec(
            MinigameRegistries.STAT_TYPES.byNameCodec(),
            type.codec.fieldOf("value"),
            "type"
        )
    }

    private fun <T> createStat(type: StatType<T>): Stat<T> {
        return Stat(type).also { stat -> stat.frozen = this.frozen }
    }
}