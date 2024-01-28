package net.casual.arcade.minigame.events.lobby

import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.utils.impl.Location
import net.casual.arcade.area.BoxedAreaConfig
import net.casual.arcade.area.PlaceableAreaConfig
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.minigame.events.lobby.ui.CountdownConfig
import net.casual.arcade.minigame.events.lobby.ui.TimerBossBarConfig
import net.minecraft.resources.ResourceKey
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public class LobbyConfig(
    public val area: PlaceableAreaConfig,
    public val spawnPosition: Vec3,
    public val spawnRotation: Vec2,
    public val dimension: ResourceKey<Level>? = null,
    public val countdown: CountdownConfig = CountdownConfig.DEFAULT,
    public val bossbar: TimerBossBarConfig = TimerBossBarConfig.DEFAULT
) {
    public fun create(level: ServerLevel): Lobby {
        val area = this.area.create(level)
        return object: Lobby {
            override val area: PlaceableArea = area

            override val spawn: Location = Location.of(spawnPosition, spawnRotation, level)

            override fun getCountdown(): Countdown {
                return countdown.create()
            }

            override fun createBossbar(): TimerBossBar {
                return bossbar.create()
            }
        }
    }

    public companion object {
        public val DEFAULT: LobbyConfig = LobbyConfig(BoxedAreaConfig.DEFAULT, Vec3(0.0, 1.0, 0.0), Vec2.ZERO)
    }
}