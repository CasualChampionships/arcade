package net.casual.arcade.utils.location.teleporter

import com.google.common.collect.Multimap
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.utils.EntityUtils.teleportTo
import net.casual.arcade.utils.location.template.LocationTemplate
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public class LocationTeleporter(
    public val location: LocationTemplate
): EntityTeleporter {
    override fun teleportEntities(level: ServerLevel, entities: List<Entity>) {
        for (entity in entities) {
            entity.teleportTo(this.location.get(level))
        }
    }

    override fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>) {
        for ((_, entities) in teams.asMap()) {
            val location = this.location.get(level)
            for (entity in entities) {
                entity.teleportTo(location)
            }
        }
    }

    override fun codec(): MapCodec<out EntityTeleporter> {
        return CODEC
    }

    public companion object: CodecProvider<LocationTeleporter> {
        override val ID: ResourceLocation = Arcade.id("location")

        override val CODEC: MapCodec<out LocationTeleporter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                LocationTemplate.CODEC.fieldOf("location").forGetter(LocationTeleporter::location)
            ).apply(instance, ::LocationTeleporter)
        }
    }
}