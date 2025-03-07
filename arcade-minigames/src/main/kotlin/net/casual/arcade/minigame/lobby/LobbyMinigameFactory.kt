/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.lobby

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.serialization.MinigameCreationContext
import net.casual.arcade.minigame.serialization.MinigameFactory
import net.casual.arcade.minigame.template.area.PlaceableAreaTemplate
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.providers.LocationProvider
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

public data class LobbyMinigameFactory(
    private val dimension: ResourceKey<Level> = Level.OVERWORLD,
    private val area: PlaceableAreaTemplate = PlaceableAreaTemplate.DEFAULT,
    private val location: LocationProvider = LocationProvider.DEFAULT
): MinigameFactory {
    override fun create(context: MinigameCreationContext): Minigame {
        val level = context.server.getLevel(this.dimension)
            ?: throw IllegalStateException("Dimension ${this.dimension} does not exist")
        return LobbyMinigame(context.server, context.uuid, this.area.create(level), this.location.get().with(level))
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
                LocationProvider.CODEC.optionalFieldOf("location", LocationProvider.DEFAULT).forGetter(LobbyMinigameFactory::location)
            ).apply(instance, ::LobbyMinigameFactory)
        }
    }
}