package net.casual.arcade.utils.registries

import com.mojang.serialization.MapCodec
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.minigame.managers.chat.MinigameChatMode
import net.casual.arcade.utils.location.teleporter.EntityTeleporter
import net.casual.arcade.utils.location.template.LocationTemplate
import net.minecraft.core.Registry

public object ArcadeRegistries: RegistrySupplier() {
    public val PLACEABLE_AREA_TEMPLATE: Registry<MapCodec<out PlaceableAreaTemplate>> = create(ArcadeRegistryKeys.PLACEABLE_AREA_TEMPLATE, PlaceableAreaTemplate::bootstrap)
    public val COUNTDOWN_TEMPLATE: Registry<MapCodec<out CountdownTemplate>> = create(ArcadeRegistryKeys.COUNTDOWN_TEMPLATE, CountdownTemplate::bootstrap)
    public val TIMER_BOSSBAR_TEMPLATE: Registry<MapCodec<out TimerBossBarTemplate>> = create(ArcadeRegistryKeys.TIMER_BOSSBAR_TEMPLATE, TimerBossBarTemplate::bootstrap)
    public val LOCATION_TEMPLATE: Registry<MapCodec<out LocationTemplate>> = create(ArcadeRegistryKeys.LOCATION_TEMPLATE, LocationTemplate::bootstrap)
    public val ENTITY_TELEPORTER: Registry<MapCodec<out EntityTeleporter>> = create(ArcadeRegistryKeys.ENTITY_TELEPORTER, EntityTeleporter::bootstrap)
    public val LOBBY_TEMPLATE: Registry<MapCodec<out LobbyTemplate>> = create(ArcadeRegistryKeys.LOBBY_TEMPLATE, LobbyTemplate::bootstrap)
    public val MINIGAME_CHAT_MODES: Registry<MapCodec<out MinigameChatMode>> = create(ArcadeRegistryKeys.MINIGAME_CHAT_MODE, MinigameChatMode::bootstrap)
    public val MINIGAMES_EVENT: Registry<MapCodec<out MinigamesEvent>> = create(ArcadeRegistryKeys.MINIGAMES_EVENT, MinigamesEvent::bootstrap)
}