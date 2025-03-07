/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.Location
import net.minecraft.resources.ResourceLocation
import kotlin.random.Random

public class AroundLocationProvider(
    private val horizontalRadius: Double,
    private val verticalRadius: Double,
    private val location: LocationProvider
): LocationProvider {
    override fun get(): Location {
        val location = this.location.get()
        val randomX = if (this.horizontalRadius == 0.0) 0.0 else Random.nextDouble(-this.horizontalRadius, this.horizontalRadius)
        val randomY = if (this.verticalRadius == 0.0) 0.0 else Random.nextDouble(-this.verticalRadius, this.verticalRadius)
        val randomZ = if (this.horizontalRadius == 0.0) 0.0 else Random.nextDouble(-this.horizontalRadius, this.horizontalRadius)
        return location.copy(position = location.position.add(randomX, randomY, randomZ))
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return CODEC
    }

    public companion object: CodecProvider<AroundLocationProvider> {
        override val ID: ResourceLocation = ResourceUtils.arcade("around")

        override val CODEC: MapCodec<out AroundLocationProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("horizontal_radius").forGetter(AroundLocationProvider::horizontalRadius),
                Codec.DOUBLE.fieldOf("vertical_radius").forGetter(AroundLocationProvider::verticalRadius),
                LocationProvider.CODEC.fieldOf("location").forGetter(AroundLocationProvider::location)
            ).apply(instance, ::AroundLocationProvider)
        }
    }
}