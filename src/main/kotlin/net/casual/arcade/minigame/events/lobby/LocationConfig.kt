package net.casual.arcade.minigame.events.lobby

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.impl.Location
import net.minecraft.server.level.ServerLevel

public class LocationConfig(
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
        public val DEFAULT: LocationConfig = LocationConfig()

        public val CODEC: Codec<LocationConfig> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.DOUBLE.encodedOptionalFieldOf("x", 0.0).forGetter(LocationConfig::x),
                Codec.DOUBLE.encodedOptionalFieldOf("y", 0.0).forGetter(LocationConfig::y),
                Codec.DOUBLE.encodedOptionalFieldOf("z", 0.0).forGetter(LocationConfig::z),
                Codec.FLOAT.encodedOptionalFieldOf("yaw", 0.0F).forGetter(LocationConfig::yaw),
                Codec.FLOAT.encodedOptionalFieldOf("pitch", 0.0F).forGetter(LocationConfig::pitch),
            ).apply(instance, ::LocationConfig)
        }
    }
}