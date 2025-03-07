/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.teleporter

import com.google.common.collect.Multimap
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.math.location.providers.LocationProvider
import net.casual.arcade.utils.teleportTo
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public class LocationTeleporter(
    public val location: LocationProvider
): EntityTeleporter {
    override fun teleportEntities(level: ServerLevel, entities: List<Entity>) {
        for (entity in entities) {
            entity.teleportTo(this.location.get().with(level))
        }
    }

    override fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>) {
        for ((_, entities) in teams.asMap()) {
            val location = this.location.get().with(level)
            for (entity in entities) {
                entity.teleportTo(location)
            }
        }
    }

    override fun codec(): MapCodec<out EntityTeleporter> {
        return CODEC
    }

    public companion object: CodecProvider<LocationTeleporter> {
        override val ID: ResourceLocation = ResourceUtils.arcade("location")

        override val CODEC: MapCodec<out LocationTeleporter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationProvider.CODEC.fieldOf("location").forGetter(LocationTeleporter::location)
            ).apply(instance, ::LocationTeleporter)
        }
    }
}