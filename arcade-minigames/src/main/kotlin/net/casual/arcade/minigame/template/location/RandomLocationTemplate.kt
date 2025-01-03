/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.location

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.impl.Location
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel

public class RandomLocationTemplate(
    private val locations: List<LocationTemplate>
): LocationTemplate {
    override fun get(level: ServerLevel): Location {
        val index = level.random.nextInt(this.locations.size)
        return this.locations[index].get(level)
    }

    override fun codec(): MapCodec<out LocationTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<RandomLocationTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("random")

        override val CODEC: MapCodec<RandomLocationTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationTemplate.CODEC.listOf(1, Int.MAX_VALUE).fieldOf("locations").forGetter(RandomLocationTemplate::locations)
            ).apply(instance, ::RandomLocationTemplate)
        }
    }
}