/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.flashback

import com.mojang.serialization.Codec
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.minecraft.resources.ResourceKey
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import java.util.Optional

public data class FlashbackMarker(
    val color: Int,
    val location: Optional<Location> = Optional.empty(),
    val description: Optional<String> = Optional.empty()
) {
    public data class Location(val position: Vec3, val dimension: String) {
        public companion object {
            public val CODEC: Codec<Location> = OrderedRecordCodecBuilder.create { instance ->
                instance.group(
                    Vec3.CODEC.fieldOf("position").forGetter(Location::position),
                    Codec.STRING.fieldOf("dimension").forGetter(Location::dimension)
                ).apply(instance, ::Location)
            }

            public fun from(position: Vec3?, dimension: ResourceKey<Level>): Location? {
                return if (position == null) null else Location(position, dimension.toString())
            }
        }
    }

    public companion object {
        public val CODEC: Codec<FlashbackMarker> = OrderedRecordCodecBuilder.create { instance ->
            instance.group(
                // Flashback uses the British spelling of colour, this is intentional
                Codec.INT.fieldOf("colour").forGetter(FlashbackMarker::color),
                Location.CODEC.optionalFieldOf("position").forGetter(FlashbackMarker::location),
                Codec.STRING.optionalFieldOf("description").forGetter(FlashbackMarker::description)
            ).apply(instance, ::FlashbackMarker)
        }
    }
}