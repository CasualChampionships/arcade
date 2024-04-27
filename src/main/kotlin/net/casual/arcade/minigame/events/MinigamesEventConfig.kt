package net.casual.arcade.minigame.events

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.minigame.events.lobby.LobbyConfig
import net.casual.arcade.utils.serialization.ResourceLocationSerializer
import net.minecraft.resources.ResourceLocation

public typealias SerializableResourceLocation = @Serializable(with = ResourceLocationSerializer::class) ResourceLocation

@Serializable
public abstract class MinigamesEventConfig {
    public abstract val lobby: LobbyConfig
    public abstract val packs: List<String>
    public abstract val operators: List<String>
    public abstract val minigames: List<SerializableResourceLocation>
    public abstract val repeat: Boolean

    public companion object {
        public val DEFAULT: MinigamesEventConfig = SimpleMinigamesEventConfig()
    }
}

@Serializable
@SerialName("simple")
public class SimpleMinigamesEventConfig(
    override val lobby: LobbyConfig = LobbyConfig.DEFAULT,
    override val packs: List<String> = listOf(),
    override val operators: List<String> = listOf(),
    override val minigames: List<SerializableResourceLocation> = listOf(),
    override val repeat: Boolean = true
): MinigamesEventConfig()