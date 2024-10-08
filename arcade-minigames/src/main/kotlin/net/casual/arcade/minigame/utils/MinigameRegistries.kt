package net.casual.arcade.minigame.utils

import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.managers.chat.MinigameChatMode
import net.casual.arcade.minigame.template.area.PlaceableAreaTemplate
import net.casual.arcade.minigame.template.bossbar.TimerBossbarTemplate
import net.casual.arcade.minigame.template.countdown.CountdownTemplate
import net.casual.arcade.minigame.template.lobby.LobbyTemplate
import net.casual.arcade.minigame.template.location.LocationTemplate
import net.casual.arcade.minigame.template.minigame.MinigamesTemplate
import net.casual.arcade.minigame.template.teleporter.EntityTeleporter
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.registries.RegistryKeySupplier
import net.casual.arcade.utils.registries.RegistrySupplier
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object MinigameRegistryKeys: RegistryKeySupplier(ArcadeUtils.MOD_ID) {
    public val PLACEABLE_AREA_TEMPLATE: ResourceKey<Registry<MapCodec<out PlaceableAreaTemplate>>> = create("placeable_area_template")
    public val COUNTDOWN_TEMPLATE: ResourceKey<Registry<MapCodec<out CountdownTemplate>>> = create("countdown_template")
    public val TIMER_BOSSBAR_TEMPLATE: ResourceKey<Registry<MapCodec<out TimerBossbarTemplate>>> = create("timer_bossbar_template")
    public val LOBBY_TEMPLATE: ResourceKey<Registry<MapCodec<out LobbyTemplate>>> = create("lobby_template")
    public val LOCATION_TEMPLATE: ResourceKey<Registry<MapCodec<out LocationTemplate>>> = create("location_template")
    public val ENTITY_TELEPORTER: ResourceKey<Registry<MapCodec<out EntityTeleporter>>> = create("entity_teleporter")
    public val MINIGAME_CHAT_MODE: ResourceKey<Registry<MapCodec<out MinigameChatMode>>> = create("minigame_chat_mode")
    public val MINIGAMES_EVENT: ResourceKey<Registry<MapCodec<out MinigamesTemplate>>> = create("minigames_event")
}

public object MinigameRegistries: RegistrySupplier() {
    public val PLACEABLE_AREA_TEMPLATE: Registry<MapCodec<out PlaceableAreaTemplate>> = create(MinigameRegistryKeys.PLACEABLE_AREA_TEMPLATE, PlaceableAreaTemplate::bootstrap)
    public val COUNTDOWN_TEMPLATE: Registry<MapCodec<out CountdownTemplate>> = create(MinigameRegistryKeys.COUNTDOWN_TEMPLATE, CountdownTemplate::bootstrap)
    public val TIMER_BOSSBAR_TEMPLATE: Registry<MapCodec<out TimerBossbarTemplate>> = create(MinigameRegistryKeys.TIMER_BOSSBAR_TEMPLATE, TimerBossbarTemplate::bootstrap)
    public val LOCATION_TEMPLATE: Registry<MapCodec<out LocationTemplate>> = create(MinigameRegistryKeys.LOCATION_TEMPLATE, LocationTemplate::bootstrap)
    public val ENTITY_TELEPORTER: Registry<MapCodec<out EntityTeleporter>> = create(MinigameRegistryKeys.ENTITY_TELEPORTER, EntityTeleporter::bootstrap)
    public val LOBBY_TEMPLATE: Registry<MapCodec<out LobbyTemplate>> = create(MinigameRegistryKeys.LOBBY_TEMPLATE, LobbyTemplate::bootstrap)
    public val MINIGAME_CHAT_MODES: Registry<MapCodec<out MinigameChatMode>> = create(MinigameRegistryKeys.MINIGAME_CHAT_MODE, MinigameChatMode::bootstrap)
    public val MINIGAMES_EVENT: Registry<MapCodec<out MinigamesTemplate>> = create(MinigameRegistryKeys.MINIGAMES_EVENT, MinigamesTemplate::bootstrap)
}