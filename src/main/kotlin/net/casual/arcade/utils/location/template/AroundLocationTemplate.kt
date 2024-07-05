package net.casual.arcade.utils.location.template

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.utils.location.Location
import net.casual.arcade.utils.serialization.CodecProvider
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
        override val ID: ResourceLocation = Arcade.id("around")
        override val CODEC: MapCodec<AroundLocationTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("horizontal_radius").forGetter(AroundLocationTemplate::horizontalRadius),
                Codec.DOUBLE.fieldOf("vertical_radius").forGetter(AroundLocationTemplate::verticalRadius),
                LocationTemplate.CODEC.fieldOf("location").forGetter(AroundLocationTemplate::location)
            ).apply(instance, ::AroundLocationTemplate)
        }
    }
}