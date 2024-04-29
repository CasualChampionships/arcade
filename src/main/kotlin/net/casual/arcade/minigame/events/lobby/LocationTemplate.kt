package net.casual.arcade.minigame.events.lobby

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.impl.Location
import net.minecraft.server.level.ServerLevel

public class LocationTemplate(
    public val x: Double = 0.0,
    public val y: Double = 0.0,
    public val z: Double = 0.0,
    public val yaw: Float = 0.0F,
    public val pitch: Float = 0.0F
) {
    public fun toLocation(level: ServerLevel): Location {
        return Location.of(this.x, this.y, this.z, this.yaw, this.pitch, level)
    }

    public companion object {
        public val DEFAULT: LocationTemplate = LocationTemplate()

        public val CODEC: Codec<LocationTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.DOUBLE.encodedOptionalFieldOf("x", 0.0).forGetter(LocationTemplate::x),
                Codec.DOUBLE.encodedOptionalFieldOf("y", 0.0).forGetter(LocationTemplate::y),
                Codec.DOUBLE.encodedOptionalFieldOf("z", 0.0).forGetter(LocationTemplate::z),
                Codec.FLOAT.encodedOptionalFieldOf("yaw", 0.0F).forGetter(LocationTemplate::yaw),
                Codec.FLOAT.encodedOptionalFieldOf("pitch", 0.0F).forGetter(LocationTemplate::pitch),
            ).apply(instance, ::LocationTemplate)
        }
    }
}