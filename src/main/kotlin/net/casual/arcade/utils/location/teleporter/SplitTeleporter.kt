package net.casual.arcade.utils.location.teleporter

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.utils.serialization.CodecProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public class SplitTeleporter(
    private val threshold: Int,
    private val primaryWeight: Int,
    private val secondaryWeight: Int,
    private val primary: EntityTeleporter,
    private val secondary: EntityTeleporter
): EntityTeleporter {
    private val primaryMultiplier = this.primaryWeight / (this.primaryWeight + this.secondaryWeight).toDouble()

    override fun teleportEntities(level: ServerLevel, entities: List<Entity>) {
        if (entities.size < this.threshold) {
            return this.primary.teleportEntities(level, entities)
        }
        val primaryCount = Mth.ceil(entities.size * this.primaryMultiplier)
        this.primary.teleportEntities(level, entities.subList(0, primaryCount))
        this.secondary.teleportEntities(level, entities.subList(primaryCount, entities.size))
    }

    override fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>) {
        val total = teams.values().size
        if (total < this.threshold) {
            return this.primary.teleportTeams(level, teams)
        }
        val secondaryCount = total - Mth.ceil(total * this.primaryMultiplier)

        val primaryTeams = HashMultimap.create<PlayerTeam, Entity>()
        val secondaryTeams = HashMultimap.create<PlayerTeam, Entity>()

        for ((team, entities) in teams.asMap()) {
            if (secondaryTeams.values().size + entities.size <= secondaryCount) {
                secondaryTeams.putAll(team, entities)
            } else {
                primaryTeams.putAll(team, entities)
            }
        }
        this.primary.teleportTeams(level, primaryTeams)
        this.secondary.teleportTeams(level, secondaryTeams)
    }

    override fun codec(): MapCodec<out EntityTeleporter> {
        return CODEC
    }

    public companion object: CodecProvider<SplitTeleporter> {
        override val ID: ResourceLocation = Arcade.id("split")

        override val CODEC: MapCodec<out SplitTeleporter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("entity_threshold").forGetter(SplitTeleporter::threshold),
                Codec.INT.optionalFieldOf("primary_weight", 1).forGetter(SplitTeleporter::primaryWeight),
                Codec.INT.optionalFieldOf("secondary_weight", 1).forGetter(SplitTeleporter::secondaryWeight),
                EntityTeleporter.CODEC.fieldOf("primary").forGetter(SplitTeleporter::primary),
                EntityTeleporter.CODEC.fieldOf("secondary").forGetter(SplitTeleporter::secondary)
            ).apply(instance, ::SplitTeleporter)
        }
    }
}