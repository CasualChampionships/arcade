package net.casual.arcade.minigame.managers

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.stats.Stat
import net.casual.arcade.stats.StatTracker
import net.casual.arcade.stats.StatType
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class MinigameStatManager {
    private val stats = HashMap<UUID, StatTracker>()
    private var frozen: Boolean = false

    public fun freeze() {
        this.frozen = true
        for (stat in this.stats.values) {
            stat.freeze()
        }
    }

    public fun unfreeze() {
        this.frozen = false
        for (stat in this.stats.values) {
            stat.unfreeze()
        }
    }

    public fun <T> getOrCreateStat(player: ServerPlayer, type: StatType<T>): Stat<T> {
        return this.getOrCreateTracker(player.uuid).getOrCreateStat(type)
    }

    public fun serialize(): JsonArray {
        val stats = JsonArray()
        for ((uuid, tracker) in this.stats) {
            val data = JsonObject()
            data.addProperty("uuid", uuid.toString())
            data.add("stats", tracker.serialize())
            stats.add(data)
        }
        return stats
    }

    public fun serialize(player: ServerPlayer): JsonArray {
        return this.serialize(player.uuid)
    }

    public fun serialize(uuid: UUID): JsonArray {
        return this.stats[uuid]?.serialize() ?: JsonArray()
    }

    internal fun deserialize(array: JsonArray) {
        for (tracker in array.objects()) {
            val uuid = UUID.fromString(tracker.string("uuid"))
            this.getOrCreateTracker(uuid).deserialize(tracker.array("stats"))
        }
    }

    private fun getOrCreateTracker(uuid: UUID): StatTracker {
        return this.stats.getOrPut(uuid) {
            StatTracker().also { if (this.frozen) it.freeze() }
        }
    }
}