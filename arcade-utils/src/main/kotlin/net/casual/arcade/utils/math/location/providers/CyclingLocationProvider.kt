package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.Location
import net.minecraft.resources.ResourceLocation

public class CyclingLocationProvider(
    private val locations: List<LocationProvider>
): LocationProvider {
    private var index = 0

    override fun get(): Location {
        return this.locations[this.index++ % this.locations.size].get()
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return CODEC
    }

    public companion object: CodecProvider<CyclingLocationProvider> {
        override val ID: ResourceLocation = ResourceUtils.arcade("cycling")

        override val CODEC: MapCodec<out CyclingLocationProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.listOf(1, Int.MAX_VALUE).fieldOf("locations").forGetter(CyclingLocationProvider::locations)
            ).apply(instance, ::CyclingLocationProvider)
        }
    }
}