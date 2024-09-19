package net.casual.arcade.minigame.stats

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.minecraft.resources.ResourceLocation
import java.util.concurrent.ConcurrentHashMap

public class StatTracker {
    private val unprocessed = ConcurrentHashMap<ResourceLocation, Pair<JsonElement, String>>()
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

    public fun <T> getStatValueOrDefault(type: StatType<T>): T {
        val unprocessed = this.unprocessed[type.id]
        if (unprocessed != null) {
            return type.serializer.deserialize(unprocessed.first)
        }

        val stat = this.stats[type] ?: return type.default
        @Suppress("UNCHECKED_CAST")
        return (stat as Stat<T>).value
    }

    public fun <T> getOrCreateStat(type: StatType<T>): Stat<T> {
        this.unprocessed.remove(type.id)?.let { (data, _) ->
            val stat = this.createStat(type)
            stat.deserialize(data)
            this.stats[type] = stat
            return stat
        }

        @Suppress("UNCHECKED_CAST")
        return this.stats.getOrPut(type) { this.createStat(type) } as Stat<T>
    }

    public fun serialize(): JsonArray {
        val stats = JsonArray()
        for ((type, stat) in this.stats) {
            val statData = JsonObject()
            statData.addProperty("type", type.id.toString())
            statData.add("value", stat.serialize())
            statData.addProperty("value_type", stat.stat.serializer.type())
            stats.add(statData)
        }
        for ((type, stat) in this.unprocessed) {
            val statData = JsonObject()
            statData.addProperty("type", type.toString())
            statData.add("value", stat.first)
            statData.addProperty("value_type", stat.second)
            stats.add(statData)
        }
        return stats
    }

    public fun deserialize(stats: JsonArray) {
        for (statData in stats.objects()) {
            val location = ResourceLocation.parse(statData.string("type"))
            val value = statData["value"]
            val type = statData.string("value_type")
            this.unprocessed[location] = value to type
        }
    }

    private fun <T> createStat(type: StatType<T>): Stat<T> {
        return Stat(type).also { stat -> stat.frozen = this.frozen }
    }
}