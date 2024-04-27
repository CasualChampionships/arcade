package net.casual.arcade.minigame.events.lobby

import kotlinx.serialization.Contextual
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.area.BoxedAreaConfig
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.area.PlaceableAreaConfig
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.minigame.events.lobby.ui.TimerBossBarConfig
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.utils.serialization.DimensionSerializer
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

@Serializable
@SerialName("default")
public open class LobbyConfig(
    public val area: PlaceableAreaConfig = BoxedAreaConfig.DEFAULT,
    public val spawn: SpawnConfig = SpawnConfig.DEFAULT,
    @Serializable(with = DimensionSerializer::class)
    public val dimension: ResourceKey<@Contextual Level>? = null,
    public val countdown: Countdown = TitledCountdown.DEFAULT,
    public val bossbar: TimerBossBarConfig = TimerBossBarConfig.DEFAULT
) {
    public fun create(level: ServerLevel): Lobby {
        val area = this.area.create(level)
        val spawn = this.spawn.location(level)
        return object: Lobby {
            override val area: PlaceableArea = area

            override val spawn: Location = spawn

            override fun getCountdown(): Countdown {
                return countdown
            }

            override fun createBossbar(): TimerBossBar {
                return bossbar.create()
            }
        }
    }

    public companion object {
        public val DEFAULT: LobbyConfig = LobbyConfig()
    }
}