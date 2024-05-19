package net.casual.arcade.utils.registries

import com.mojang.serialization.Codec
import com.mojang.serialization.Lifecycle
import net.casual.arcade.area.templates.PlaceableAreaTemplate
import net.casual.arcade.gui.countdown.templates.CountdownTemplate
import net.casual.arcade.minigame.events.MinigamesEvent
import net.casual.arcade.minigame.events.lobby.templates.LobbyTemplate
import net.casual.arcade.gui.bossbar.templates.TimerBossBarTemplate
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public object ArcadeRegistries {
    private val LOADERS = ArrayList<() -> Unit>()

    public val PLACEABLE_AREA_TEMPLATE: Registry<Codec<out PlaceableAreaTemplate>> = create(ArcadeRegistryKeys.PLACEABLE_AREA_TEMPLATE, PlaceableAreaTemplate::bootstrap)
    public val COUNTDOWN_TEMPLATE: Registry<Codec<out CountdownTemplate>> = create(ArcadeRegistryKeys.COUNTDOWN_TEMPLATE, CountdownTemplate::bootstrap)
    public val TIMER_BOSSBAR_TEMPLATE: Registry<Codec<out TimerBossBarTemplate>> = create(ArcadeRegistryKeys.TIMER_BOSSBAR_TEMPLATE, TimerBossBarTemplate::bootstrap)
    public val LOBBY_TEMPLATE: Registry<Codec<out LobbyTemplate>> = create(ArcadeRegistryKeys.LOBBY_TEMPLATE, LobbyTemplate::bootstrap)
    public val MINIGAMES_EVENT: Registry<Codec<out MinigamesEvent>> = create(ArcadeRegistryKeys.MINIGAMES_EVENT, MinigamesEvent::bootstrap)

    init {
        for (load in LOADERS) {
            load.invoke()
        }
        LOADERS.clear()
    }

    internal fun noop() {

    }

    private fun <T> create(key: ResourceKey<Registry<T>>, bootstrap: (Registry<T>) -> Unit): Registry<T> {
        val registry = MappedRegistry(key, Lifecycle.stable(), false)
        LOADERS.add { bootstrap.invoke(registry) }
        return registry
    }
}