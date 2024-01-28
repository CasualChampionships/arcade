package net.casual.arcade.minigame.events.lobby

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.casual.arcade.Arcade
import net.casual.arcade.area.BoxedAreaConfig
import net.casual.arcade.utils.JsonUtils.double
import net.casual.arcade.utils.JsonUtils.float
import net.casual.arcade.utils.JsonUtils.obj
import net.casual.arcade.utils.JsonUtils.set
import net.casual.arcade.utils.JsonUtils.string
import net.casual.arcade.utils.json.JsonSerializer
import net.casual.arcade.area.PlaceableAreaConfigFactory
import net.casual.arcade.minigame.events.lobby.ui.*
import net.casual.arcade.utils.JsonUtils.objOrNull
import net.casual.arcade.utils.JsonUtils.stringOrNull
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public class LobbyConfigSerializer: JsonSerializer<LobbyConfig> {
    private val areaFactories = HashMap<String, PlaceableAreaConfigFactory>()
    private val bossbarFactories = HashMap<String, TimerBossBarConfigFactory>()
    private val countdownFactories = HashMap<String, CountdownConfigFactory>()

    init {
        this.addAreaFactory(BoxedAreaConfig)
        this.addBossbarFactory(SimpleTimerBossbarConfig)
        this.addCountdownFactory(TitledCountdownConfig)
    }

    public fun addAreaFactory(factory: PlaceableAreaConfigFactory) {
        this.areaFactories[factory.id] = factory
    }

    public fun addBossbarFactory(factory: TimerBossBarConfigFactory) {
        this.bossbarFactories[factory.id] = factory
    }

    public fun addCountdownFactory(factory: CountdownConfigFactory) {
        this.countdownFactories[factory.id] = factory
    }

    override fun deserialize(json: JsonElement): LobbyConfig {
        json as JsonObject
        val area = json.obj("area").let { area ->
            val type = area.string("type")
            val factory = this.areaFactories[type]
            if (factory == null) {
                Arcade.logger.error("Unknown lobby area factory $type")
                return LobbyConfig.DEFAULT
            }
            factory.create(area.obj("data"))
        }

        val spawn = json.obj("spawn")
        val x = spawn.double("x")
        val y = spawn.double("y")
        val z = spawn.double("z")
        val yaw = spawn.float("yaw")
        val pitch = spawn.float("pitch")

        val dimension = json.stringOrNull("dimension")?.let {
            runCatching { ResourceKey.create(Registries.DIMENSION, ResourceLocation(it)) }.getOrNull()
        }

        val bossbar = json.objOrNull("bossbar")?.let { bossbar ->
            val type = bossbar.stringOrNull("type") ?: return@let null
            val factory = this.bossbarFactories[type]
            val data = bossbar.objOrNull("data") ?: return@let null
            factory?.create(data)
        } ?: TimerBossBarConfig.DEFAULT
        val countdown = json.objOrNull("countdown")?.let { countdown ->
            val type = countdown.stringOrNull("type") ?: return@let null
            val factory = this.countdownFactories[type]
            val data = countdown.objOrNull("data") ?: return@let null
            factory?.create(data)
        } ?: CountdownConfig.DEFAULT

        return LobbyConfig(area, Vec3(x, y, z), Vec2(pitch, yaw), dimension, countdown, bossbar)
    }

    override fun serialize(value: LobbyConfig): JsonElement {
        val json = JsonObject()
        JsonObject().also { area ->
            area["type"] = value.area.id
            area["data"] = value.area.write()
            json["area"] = area
        }
        JsonObject().also { bossbar ->
            bossbar["type"] = value.bossbar.id
            bossbar["data"] = value.bossbar.write()
            json["bossbar"] = bossbar
        }
        JsonObject().also { countdown ->
            countdown["type"] = value.countdown.id
            countdown["data"] = value.countdown.write()
            json["bossbar"] = countdown
        }

        json["dimension"] = value.dimension?.location()?.toString()

        val spawn = JsonObject()
        spawn["x"] = value.spawnPosition.x
        spawn["y"] = value.spawnPosition.y
        spawn["z"] = value.spawnPosition.z
        spawn["yaw"] = value.spawnRotation.y
        spawn["pitch"] = value.spawnRotation.x

        json["spawn"] = spawn
        return json
    }
}