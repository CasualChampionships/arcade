package net.casual.arcade.utils.serialization

import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.plus
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass
import net.casual.arcade.area.BoxedAreaConfig
import net.casual.arcade.area.PlaceableAreaConfig
import net.casual.arcade.area.StructuredAreaConfig
import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.StaticTitledCountdown
import net.casual.arcade.minigame.events.MinigamesEventConfig
import net.casual.arcade.minigame.events.SimpleMinigamesEventConfig
import net.casual.arcade.minigame.events.lobby.LobbyConfig
import net.casual.arcade.minigame.events.lobby.SimpleLobbyConfig
import net.casual.arcade.minigame.events.lobby.ui.SimpleTimerBossbarConfig
import net.casual.arcade.minigame.events.lobby.ui.TimerBossBarConfig

public object ArcadeSerializerModules {
    public fun areaModules(): SerializersModule = SerializersModule {
        polymorphic(PlaceableAreaConfig::class) {
            subclass(BoxedAreaConfig::class)
            subclass(StructuredAreaConfig::class)
        }
    }

    public fun countdownModules(): SerializersModule = SerializersModule {
        polymorphic(Countdown::class) {
            subclass(StaticTitledCountdown::class)
        }
    }

    public fun bossbarModules(): SerializersModule = SerializersModule {
        polymorphic(TimerBossBarConfig::class) {
            subclass(SimpleTimerBossbarConfig::class)
        }
    }

    public fun lobbyModules(): SerializersModule {
        return areaModules() + countdownModules() + bossbarModules() + SerializersModule {
            polymorphic(LobbyConfig::class) {
                subclass(SimpleLobbyConfig::class)
            }
        }
    }

    public fun eventModules(): SerializersModule {
        return lobbyModules() + SerializersModule {
            polymorphic(MinigamesEventConfig::class) {
                subclass(SimpleMinigamesEventConfig::class)
            }
        }
    }
}