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

public typealias SerializableDimension = @Serializable(with = DimensionSerializer::class) ResourceKey<@Contextual Level>

@Serializable
public abstract class LobbyConfig {
    public abstract val area: PlaceableAreaConfig
    public abstract val spawn: LocationConfig
    public abstract val dimension: SerializableDimension?
    public abstract val countdown: Countdown
    public abstract val bossbar: TimerBossBarConfig

    public open fun create(level: ServerLevel): Lobby {
        val area = this.area.create(level)
        val spawn = Location.of(this.spawn.x, this.spawn.y, this.spawn.z, this.spawn.yaw, this.spawn.pitch, level)
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
        public val DEFAULT: LobbyConfig = SimpleLobbyConfig()
    }
}

@Serializable
@SerialName("simple")
public class SimpleLobbyConfig(
    override val area: PlaceableAreaConfig = BoxedAreaConfig.DEFAULT,
    override val spawn: LocationConfig = LocationConfig(),
    override val dimension: SerializableDimension? = null,
    override val countdown: Countdown = TitledCountdown.DEFAULT,
    override val bossbar: TimerBossBarConfig = TimerBossBarConfig.DEFAULT
): LobbyConfig()