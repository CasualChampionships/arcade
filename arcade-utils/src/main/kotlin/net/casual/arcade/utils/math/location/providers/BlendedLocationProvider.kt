package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.Location
import net.minecraft.resources.ResourceLocation

public class BlendedLocationProvider(
    private val first: LocationProvider,
    private val second: LocationProvider,
    private val takeFirstX: Boolean,
    private val takeFirstY: Boolean,
    private val takeFirstZ: Boolean,
    private val takeFirstYaw: Boolean,
    private val takeFirstPitch: Boolean
): LocationProvider {
    override fun get(): Location {
        return this.blend(this.first.get(), this.second.get())
    }

    override fun get(count: Int): List<Location> {
        return this.first.get(count).zip(this.second.get(count)).map { this.blend(it.first, it.second) }
    }

    override fun get(origin: Location): Location {
        return this.blend(this.first.get(origin), this.second.get(origin))
    }

    override fun get(origin: Location, count: Int): List<Location> {
        return this.first.get(origin, count).zip(this.second.get(origin, count)).map { this.blend(it.first, it.second) }
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return CODEC
    }

    private fun blend(first: Location, second: Location): Location {
        val x = if (this.takeFirstX) first.x else second.x
        val y = if (this.takeFirstY) first.y else second.y
        val z = if (this.takeFirstZ) first.z else second.z
        val yaw = if (this.takeFirstYaw) first.yRot else second.yRot
        val pitch = if (this.takeFirstPitch) first.xRot else second.xRot
        return Location(x, y, z, yaw, pitch)
    }

    private fun takeFirstPosition(): Boolean {
        return this.takeFirstX && this.takeFirstY && this.takeFirstZ
    }

    private fun takeFirstRotation(): Boolean {
        return this.takeFirstYaw && this.takeFirstPitch
    }

    public companion object: CodecProvider<BlendedLocationProvider> {
        override val ID: ResourceLocation = ResourceUtils.arcade("blended")

        private val SIMPLE_CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.fieldOf("first").forGetter(BlendedLocationProvider::first),
                LocationProvider.CODEC.fieldOf("second").forGetter(BlendedLocationProvider::second),
                Codec.BOOL.optionalFieldOf("take_first_position", false).forGetter(BlendedLocationProvider::takeFirstPosition),
                Codec.BOOL.optionalFieldOf("take_first_rotation", false).forGetter(BlendedLocationProvider::takeFirstRotation)
            ).apply(instance) { first, second, pos, rot -> BlendedLocationProvider(first, second, pos, pos, pos, rot, rot) }
        }

        private val VERBOSE_CODEC = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.fieldOf("first").forGetter(BlendedLocationProvider::first),
                LocationProvider.CODEC.fieldOf("second").forGetter(BlendedLocationProvider::second),
                Codec.BOOL.optionalFieldOf("take_first_x", false).forGetter(BlendedLocationProvider::takeFirstX),
                Codec.BOOL.optionalFieldOf("take_first_y", false).forGetter(BlendedLocationProvider::takeFirstY),
                Codec.BOOL.optionalFieldOf("take_first_z", false).forGetter(BlendedLocationProvider::takeFirstZ),
                Codec.BOOL.optionalFieldOf("take_first_yaw", false).forGetter(BlendedLocationProvider::takeFirstYaw),
                Codec.BOOL.optionalFieldOf("take_first_pitch", false).forGetter(BlendedLocationProvider::takeFirstPitch)
            ).apply(instance, ::BlendedLocationProvider)
        }

        override val CODEC: MapCodec<out BlendedLocationProvider> = ArcadeExtraCodecs.mapWithAlternative(VERBOSE_CODEC, SIMPLE_CODEC)
    }
}