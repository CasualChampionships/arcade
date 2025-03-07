/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.math.location

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public data class Location(
    public val position: Vec3,
    public val rotation: Vec2
) {
    public val x: Double get() = this.position.x
    public val y: Double get() = this.position.y
    public val z: Double get() = this.position.z

    public val xRot: Float get() = this.rotation.x
    public val yRot: Float get() = this.rotation.y

    public fun <L: Level> with(level: L): LocationWithLevel<L> {
        return LocationWithLevel(this, level)
    }

    public companion object {
        @JvmField public val DEFAULT: Location = Location(Vec3.ZERO, Vec2.ZERO)

        private val SIMPLE_MAP_CODEC: MapCodec<Location> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Vec3.CODEC.fieldOf("position").forGetter(Location::position),
                ArcadeExtraCodecs.VEC2.fieldOf("rotation").forGetter(Location::rotation)
            ).apply(instance, ::Location)
        }

        private val VERBOSE_MAP_CODEC: MapCodec<Location> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.DOUBLE.fieldOf("x").forGetter { it.position.x },
                Codec.DOUBLE.fieldOf("y").forGetter { it.position.y },
                Codec.DOUBLE.fieldOf("z").forGetter { it.position.z },
                Codec.FLOAT.fieldOf("yaw").forGetter { it.rotation.y },
                Codec.FLOAT.fieldOf("pitch").forGetter { it.rotation.x },
            ).apply(instance) { x, y, z, yaw, pitch -> Location(Vec3(x, y, z), Vec2(pitch, yaw)) }
        }

        public val MAP_CODEC: MapCodec<Location> = ArcadeExtraCodecs.mapWithAlternative(SIMPLE_MAP_CODEC, VERBOSE_MAP_CODEC)

        public val CODEC: Codec<Location> = MAP_CODEC.codec()

        public val Entity.location: Location
            get() = Location(this.position(), this.rotationVector)

        public fun Vec3.withRotation(rotation: Vec2): Location {
            return Location(this, rotation)
        }

        public fun Vec2.withPosition(position: Vec3): Location {
            return Location(position, this)
        }
    }
}