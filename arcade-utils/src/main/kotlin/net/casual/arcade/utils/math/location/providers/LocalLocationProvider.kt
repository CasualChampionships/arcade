package net.casual.arcade.utils.math.location.providers

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.MathUtils.component1
import net.casual.arcade.utils.MathUtils.component2
import net.casual.arcade.utils.MathUtils.component3
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.Location
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import kotlin.math.cos
import kotlin.math.sin

public class LocalLocationProvider(
    private val offsets: Vec3
): LocationProvider {
    override fun get(): Location {
        return this.get(Location.DEFAULT)
    }

    override fun get(origin: Location): Location {
        val yaw = Math.toRadians(origin.rotation.y + 90.0)
        val cosYaw = cos(yaw)
        val sinYaw = sin(yaw)

        val negPitch = Math.toRadians(0.0 - origin.rotation.x)
        val cosNegPitch = cos(negPitch)
        val sinNegPitch = sin(negPitch)

        val negPitchPlus90Rad = Math.toRadians(-origin.rotation.x + 90.0)
        val cosNegPitchPlus90 = cos(negPitchPlus90Rad)
        val sinNegPitchPlus90 = sin(negPitchPlus90Rad)

        val forwardDir = Vec3(cosYaw * cosNegPitch, sinNegPitch, sinYaw * cosNegPitch)
        val upDir = Vec3(cosYaw * cosNegPitchPlus90, sinNegPitchPlus90, sinYaw * cosNegPitchPlus90)
        val rightDir = forwardDir.cross(upDir).reverse()

        val (offsetX, offsetY, offsetZ) = this.offsets
        val worldOffsetX = forwardDir.x * offsetZ + upDir.x * offsetY + rightDir.x * offsetX
        val worldOffsetY = forwardDir.y * offsetZ + upDir.y * offsetY + rightDir.y * offsetX
        val worldOffsetZ = forwardDir.z * offsetZ + upDir.z * offsetY + rightDir.z * offsetX

        val position = origin.position.add(worldOffsetX, worldOffsetY, worldOffsetZ)
        return Location(position, Vec2.ZERO)
    }

    override fun codec(): MapCodec<out LocationProvider> {
        return CODEC
    }

    public companion object: CodecProvider<LocalLocationProvider> {
        override val ID: ResourceLocation = ResourceUtils.arcade("local")

        override val CODEC: MapCodec<LocalLocationProvider> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Vec3.CODEC.fieldOf("offsets").forGetter(LocalLocationProvider::offsets)
            ).apply(instance, ::LocalLocationProvider)
        }
    }
}