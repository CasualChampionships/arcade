package net.casual.arcade.stats

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.minecraft.resources.ResourceLocation

public class StatTracker {
    private val unprocessed = HashMap<ResourceLocation, Pair<JsonElement, String>>()
    private val stats = HashMap<StatType<*>, Stat<*>>()

    public fun <T> getOrCreateStat(type: StatType<T>): Stat<T> {
        this.unprocessed.remove(type.id)?.let { (data, _) ->
            val stat = Stat(type)
            stat.deserialize(data)
            this.stats[type] = stat
            return stat
        }

        @Suppress("UNCHECKED_CAST")
        return this.stats.getOrPut(type) { Stat(type) } as Stat<T>
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
            val location = ResourceLocation(statData.string("type"))
            val value = statData["value"]
            val type = statData.string("value_type")
            this.unprocessed[location] = value to type
        }
    }
}