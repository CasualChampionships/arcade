package net.casual.arcade.minigame.events

import net.casual.arcade.minigame.events.lobby.LobbyConfig
import net.minecraft.resources.ResourceLocation

public data class MinigamesEventConfig(
    val teamSize: Int,
    val lobby: LobbyConfig,
    val packs: List<String>,
    val operators: List<String>,
    val minigames: List<ResourceLocation>
) {
    public companion object {
        public val DEFAULT: MinigamesEventConfig = MinigamesEventConfig(5, LobbyConfig.DEFAULT, listOf(), listOf(), listOf())
    }
}