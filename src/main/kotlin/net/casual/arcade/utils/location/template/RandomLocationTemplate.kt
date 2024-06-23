package net.casual.arcade.utils.location.template

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.utils.location.Location
import net.casual.arcade.utils.serialization.CodecProvider
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
        override val ID: ResourceLocation = Arcade.id("random")

        override val CODEC: MapCodec<out RandomLocationTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationTemplate.CODEC.listOf(1, Int.MAX_VALUE).fieldOf("locations").forGetter(RandomLocationTemplate::locations)
            ).apply(instance, ::RandomLocationTemplate)
        }
    }
}