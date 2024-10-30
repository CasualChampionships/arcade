package net.casual.arcade.minigame.lobby

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.template.lobby.LobbyTemplate
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

public class LobbyMinigameFactory(
    private val lobby: LobbyTemplate,
    private val level: ResourceKey<Level>
): MinigameFactory {
    override fun create(context: MinigameCreationContext): Minigame {
        val level = context.server.getLevel(this.level) ?: context.server.overworld()
        return LobbyMinigame(context.server, context.uuid, this.lobby.create(level))
    }

    override fun codec(): MapCodec<out MinigameFactory> {
        return CODEC
    }

    public companion object: CodecProvider<LobbyMinigameFactory> {
        override val ID: ResourceLocation = ResourceUtils.arcade("lobby")

        override val CODEC: MapCodec<out LobbyMinigameFactory> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LobbyTemplate.CODEC.optionalFieldOf("lobby", LobbyTemplate.DEFAULT).forGetter(LobbyMinigameFactory::lobby),
                Level.RESOURCE_KEY_CODEC.optionalFieldOf("level", Level.OVERWORLD).forGetter(LobbyMinigameFactory::level)
            ).apply(instance, ::LobbyMinigameFactory)
        }
    }
}