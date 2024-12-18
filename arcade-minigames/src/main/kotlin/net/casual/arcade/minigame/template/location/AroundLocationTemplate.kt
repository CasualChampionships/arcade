/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.location

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.impl.Location
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import kotlin.random.Random

public class AroundLocationTemplate(
    public val horizontalRadius: Double,
    public val verticalRadius: Double,
    public val location: LocationTemplate
): LocationTemplate {
    override fun get(level: ServerLevel): Location {
        val location = this.location.get(level)
        val randomX = if (this.horizontalRadius == 0.0) 0.0 else Random.nextDouble(-this.horizontalRadius, this.horizontalRadius)
        val randomY = if (this.verticalRadius == 0.0) 0.0 else Random.nextDouble(-this.verticalRadius, this.verticalRadius)
        val randomZ = if (this.horizontalRadius == 0.0) 0.0 else Random.nextDouble(-this.horizontalRadius, this.horizontalRadius)
        return location.copy(position = location.position.add(randomX, randomY, randomZ))
    }

    override fun codec(): MapCodec<out LocationTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<AroundLocationTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("around")
        override val CODEC: MapCodec<AroundLocationTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("horizontal_radius").forGetter(AroundLocationTemplate::horizontalRadius),
                Codec.DOUBLE.fieldOf("vertical_radius").forGetter(AroundLocationTemplate::verticalRadius),
                LocationTemplate.CODEC.fieldOf("location").forGetter(AroundLocationTemplate::location)
            ).apply(instance, ::AroundLocationTemplate)
        }
    }
}