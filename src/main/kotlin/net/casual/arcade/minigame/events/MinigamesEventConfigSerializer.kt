package net.casual.arcade.minigame.events

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.utils.JsonUtils.any
import net.casual.arcade.utils.JsonUtils.arrayOrDefault
import net.casual.arcade.utils.JsonUtils.intOrDefault
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.strings
import net.casual.arcade.utils.JsonUtils.toJsonStringArray
import net.casual.arcade.utils.json.JsonSerializer
import net.casual.arcade.area.PlaceableAreaConfigFactory
import net.casual.arcade.minigame.events.lobby.LobbyConfig
import net.casual.arcade.minigame.events.lobby.LobbyConfigSerializer
import net.casual.arcade.minigame.events.lobby.ui.CountdownConfigFactory
import net.casual.arcade.minigame.events.lobby.ui.TimerBossbarConfigFactory
import net.minecraft.resources.ResourceLocation

public class MinigamesEventConfigSerializer: JsonSerializer<MinigamesEventConfig> {
    private val lobbySerializer = LobbyConfigSerializer()

    public fun addAreaFactory(factory: PlaceableAreaConfigFactory) {
        this.lobbySerializer.addAreaFactory(factory)
    }

    public fun addBossbarFactory(factory: TimerBossbarConfigFactory) {
        this.lobbySerializer.addBossbarFactory(factory)
    }

    public fun addCountdownFactory(factory: CountdownConfigFactory) {
        this.lobbySerializer.addCountdownFactory(factory)
    }

    override fun deserialize(json: JsonElement): MinigamesEventConfig {
        json as JsonObject
        // TODO: Make teams more customisable
        val teamSize = json.intOrDefault("team_size", 5)
        val packs = json.arrayOrDefault("additional_packs").strings().toList()
        val operators = json.arrayOrDefault("operators").strings().toList()
        val minigames = json.arrayOrDefault("minigames").strings().map { ResourceLocation(it) }
        val lobby = runCatching {
            json.any("lobby", this.lobbySerializer)
        }.getOrDefault(LobbyConfig.DEFAULT)
        return MinigamesEventConfig(teamSize, lobby, packs, operators, minigames)
    }

    override fun serialize(value: MinigamesEventConfig): JsonElement {
        val json = JsonObject()
        json["team_size"] = value.teamSize
        json["additional_packs"] = value.packs.toJsonStringArray { it }
        json["operators"] = value.operators.toJsonStringArray { it }
        json["minigames"] = value.minigames.toJsonStringArray { it.toString() }
        json["lobby"] = this.lobbySerializer.serialize(value.lobby)
        return json
    }
}