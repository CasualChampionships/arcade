package net.casual.arcade.utils.registries

import com.mojang.serialization.Codec
import net.casual.arcade.Arcade
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object ArcadeRegistryKeys {
    public val PLACEABLE_AREA_TEMPLATE: ResourceKey<Registry<Codec<out PlaceableAreaTemplate>>> = create("placeable_area_template")
    public val COUNTDOWN_TEMPLATE: ResourceKey<Registry<Codec<out CountdownTemplate>>> = create("countdown_template")
    public val TIMER_BOSSBAR_TEMPLATE: ResourceKey<Registry<Codec<out TimerBossBarTemplate>>> = create("timer_bossbar_template")
    public val LOBBY_TEMPLATE: ResourceKey<Registry<Codec<out LobbyTemplate>>> = create("lobby_template")
    public val MINIGAMES_EVENT: ResourceKey<Registry<Codec<out MinigamesEvent>>> = create("minigames_event")

    private fun <T> create(path: String): ResourceKey<Registry<T>> {
        return ResourceKey.createRegistryKey(Arcade.id(path))
    }
}