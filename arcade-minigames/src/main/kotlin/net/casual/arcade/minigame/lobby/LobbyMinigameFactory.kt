package net.casual.arcade.minigame.lobby

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.template.area.PlaceableAreaTemplate
import net.casual.arcade.minigame.template.location.LocationTemplate
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

public data class LobbyMinigameFactory(
    private val dimension: ResourceKey<Level> = Level.OVERWORLD,
    private val area: PlaceableAreaTemplate = PlaceableAreaTemplate.DEFAULT,
    private val location: LocationTemplate = LocationTemplate.DEFAULT
): MinigameFactory {
    override fun create(context: MinigameCreationContext): Minigame {
        val level = context.server.getLevel(this.dimension)
            ?: throw IllegalStateException("Dimension ${this.dimension} does not exist")
        return LobbyMinigame(context.server, context.uuid, this.area.create(level), this.location.get(level))
    }

    override fun codec(): MapCodec<out MinigameFactory> {
        return CODEC
    }

    public companion object: CodecProvider<LobbyMinigameFactory> {
        public val DEFAULT: LobbyMinigameFactory = LobbyMinigameFactory()

        override val ID: ResourceLocation = ResourceUtils.arcade("lobby")

        override val CODEC: MapCodec<out LobbyMinigameFactory> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Level.RESOURCE_KEY_CODEC.optionalFieldOf("dimension", Level.OVERWORLD).forGetter(LobbyMinigameFactory::dimension),
                PlaceableAreaTemplate.CODEC.optionalFieldOf("area", PlaceableAreaTemplate.DEFAULT).forGetter(LobbyMinigameFactory::area),
                LocationTemplate.CODEC.optionalFieldOf("location", LocationTemplate.DEFAULT).forGetter(LobbyMinigameFactory::location)
            ).apply(instance, ::LobbyMinigameFactory)
        }
    }
}