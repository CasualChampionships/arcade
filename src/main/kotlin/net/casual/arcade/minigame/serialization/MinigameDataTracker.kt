package net.casual.arcade.minigame.serialization

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.utils.JsonUtils.array
import net.casual.arcade.utils.JsonUtils.long
import net.casual.arcade.utils.JsonUtils.objects
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.JsonUtils.uuid
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.server.level.ServerPlayer
import java.util.*

public class MinigameDataTracker(
    private val minigame: Minigame<*>
) {
    private val players = HashMap<UUID, JsonObject>()
    private var startTime = 0L
    private var endTime = 0L

    public fun start() {
        this.startTime = System.currentTimeMillis()
    }

    public fun end() {
        this.endTime = System.currentTimeMillis()
    }

    public fun updatePlayer(player: ServerPlayer) {
        val json = JsonObject()
        json.addProperty("uuid", player.stringUUID)
        json.add("stats", this.minigame.stats.serialize(player))
        val array = JsonArray()
        for (advancement in this.minigame.advancements.all()) {
            if (!player.advancements.getOrStartProgress(advancement).isDone) {
                continue
            }
            val display = advancement.display ?: continue
            val data = JsonObject()
            data.addProperty("id", advancement.id.toString())
            json.addProperty("item", BuiltInRegistries.ITEM.getKey(display.icon.item).toString())
            json.addProperty("title", display.title.string)
        }
        json.add("advancements", array)

        this.players[player.uuid] = json
    }

    public fun toJson(): JsonObject {
        val json = JsonObject()
        json.addProperty("minigame_start_ms", this.startTime)
        json.addProperty("minigame_end_ms", this.endTime)
        json.addProperty("id", this.minigame.id.toString())
        json.addProperty("uuid", this.minigame.uuid.toString())
        val players = JsonArray()
        for (player in this.players.values) {
            players.add(player)
        }
        json.add("players", players)
        return json
    }

    internal fun fromJson(json: JsonObject) {
        this.startTime = json.long("minigame_start_ms")
        this.endTime = json.long("minigame_end_ms")

        for (player in json.array("players").objects()) {
            this.players[player.uuid("uuid")] = player
        }
    }
}