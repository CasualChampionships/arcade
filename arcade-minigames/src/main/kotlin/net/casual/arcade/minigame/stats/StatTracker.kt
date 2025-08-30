/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.stats

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import net.casual.arcade.utils.JsonUtils.objects
import net.minecraft.core.Holder
import java.util.concurrent.ConcurrentHashMap

public class StatTracker {
    private val stats = ConcurrentHashMap<Holder<StatType<*>>, Stat<*>>()
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

    @Suppress("UNCHECKED_CAST")
    public fun <T> getStatValueOrDefault(holder: Holder<StatType<T>>): T {
        val stat = this.stats[holder as Holder<StatType<*>>] ?: return holder.value().default
        return (stat as Stat<T>).value
    }

    @Suppress("UNCHECKED_CAST")
    public fun <T> getOrCreateStat(holder: Holder<StatType<T>>): Stat<T> {
        return this.stats.getOrPut(holder as Holder<StatType<*>>) {
            this.createStat(holder.value())
        } as Stat<T>
    }

    public fun serialize(): JsonElement {
        val stats = JsonArray()
        val mapped = this.stats.mapValues { (_, v) -> v.value }
        CODEC.encodeStart(JsonOps.INSTANCE)
        for ((type, stat) in this.stats) {
            val statData = JsonObject()
//            statData.addProperty("type", type.id.toString())
//            statData.add("value", stat.serialize())
//            statData.addProperty("value_type", stat.type.codec.type())
//            stats.add(statData)
        }
        return stats
    }

    public fun deserialize(stats: JsonElement) {

        for (statData in stats.objects()) {
//            val location = ResourceLocation.parse(statData.string("type"))
//            val value = statData["value"]
//            val type = statData.string("value_type")
//            this.unprocessed[location] = value to type
        }
    }

    private fun <T> createStat(type: StatType<T>): Stat<T> {
        return Stat(type).also { stat -> stat.frozen = this.frozen }
    }

    private companion object {
        val CODEC = Codec.dispatchedMap(StatType.HOLDER_CODEC) { type -> type.value().codec }!!
    }
}