package net.casual.arcade.utils.registries

import com.mojang.serialization.MapCodec
import net.casual.arcade.Arcade
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.utils.location.teleporter.EntityTeleporter
import net.casual.arcade.utils.location.template.LocationTemplate
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object ArcadeRegistryKeys {
    public val PLACEABLE_AREA_TEMPLATE: ResourceKey<Registry<MapCodec<out PlaceableAreaTemplate>>> = create("placeable_area_template")
    public val COUNTDOWN_TEMPLATE: ResourceKey<Registry<MapCodec<out CountdownTemplate>>> = create("countdown_template")
    public val TIMER_BOSSBAR_TEMPLATE: ResourceKey<Registry<MapCodec<out TimerBossBarTemplate>>> = create("timer_bossbar_template")
    public val LOBBY_TEMPLATE: ResourceKey<Registry<MapCodec<out LobbyTemplate>>> = create("lobby_template")
    public val LOCATION_TEMPLATE: ResourceKey<Registry<MapCodec<out LocationTemplate>>> = create("location_template")
    public val ENTITY_TELEPORTER: ResourceKey<Registry<MapCodec<out EntityTeleporter>>> = create("entity_teleporter")
    public val MINIGAMES_EVENT: ResourceKey<Registry<MapCodec<out MinigamesEvent>>> = create("minigames_event")

    private fun <T> create(path: String): ResourceKey<Registry<T>> {
        return ResourceKey.createRegistryKey(Arcade.id(path))
    }
}