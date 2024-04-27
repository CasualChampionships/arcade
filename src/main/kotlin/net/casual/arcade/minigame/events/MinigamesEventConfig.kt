package net.casual.arcade.minigame.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.minigame.events.lobby.LobbyConfig
import net.casual.arcade.utils.serialization.ResourceLocationSerializer
import net.minecraft.resources.ResourceLocation

@Serializable
@SerialName("default")
public open class MinigamesEventConfig(
    public val lobby: LobbyConfig = LobbyConfig.DEFAULT,
    public val packs: List<String> = listOf(),
    public val operators: List<String> = listOf(),
    public val minigames: List<@Serializable(with = ResourceLocationSerializer::class) ResourceLocation> = listOf(),
    public val repeat: Boolean = true
) {
    public companion object {
        public val DEFAULT: MinigamesEventConfig = MinigamesEventConfig()
    }
}