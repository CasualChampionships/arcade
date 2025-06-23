/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.teleporter

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.Mth
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public class SplitTeleporter(
    private val entityThreshold: Int,
    private val teamThreshold: Int,
    private val primaryWeight: Int,
    private val secondaryWeight: Int,
    private val primary: EntityTeleporter,
    private val secondary: EntityTeleporter
): EntityTeleporter {
    private val primaryMultiplier = this.primaryWeight / (this.primaryWeight + this.secondaryWeight).toDouble()

    override fun teleportEntities(level: ServerLevel, entities: List<Entity>) {
        if (entities.size < this.entityThreshold) {
            return this.primary.teleportEntities(level, entities)
        }
        val primaryCount = Mth.ceil(entities.size * this.primaryMultiplier)
        this.primary.teleportEntities(level, entities.subList(0, primaryCount))
        this.secondary.teleportEntities(level, entities.subList(primaryCount, entities.size))
    }

    override fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>) {
        val total = teams.keySet().size
        if (total < this.teamThreshold) {
            return this.primary.teleportTeams(level, teams)
        }
        val primaryCount = Mth.ceil(total * this.primaryMultiplier)

        val primaryTeams = HashMultimap.create<PlayerTeam, Entity>()
        val secondaryTeams = HashMultimap.create<PlayerTeam, Entity>()

        for ((team, entities) in teams.asMap()) {
            if (primaryTeams.keySet().size < primaryCount) {
                primaryTeams.putAll(team, entities)
            } else {
                secondaryTeams.putAll(team, entities)
            }
        }
        this.primary.teleportTeams(level, primaryTeams)
        this.secondary.teleportTeams(level, secondaryTeams)
    }

    override fun codec(): MapCodec<out EntityTeleporter> {
        return CODEC
    }

    public companion object: CodecProvider<SplitTeleporter> {
        override val ID: ResourceLocation = ResourceUtils.arcade("split")

        override val CODEC: MapCodec<out SplitTeleporter> = OrderedRecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Codec.INT.fieldOf("entity_threshold").forGetter(SplitTeleporter::entityThreshold),
                Codec.INT.fieldOf("team_threshold").forGetter(SplitTeleporter::teamThreshold),
                Codec.INT.optionalFieldOf("primary_weight", 1).forGetter(SplitTeleporter::primaryWeight),
                Codec.INT.optionalFieldOf("secondary_weight", 1).forGetter(SplitTeleporter::secondaryWeight),
                EntityTeleporter.CODEC.fieldOf("primary").forGetter(SplitTeleporter::primary),
                EntityTeleporter.CODEC.fieldOf("secondary").forGetter(SplitTeleporter::secondary)
            ).apply(instance, ::SplitTeleporter)
        }
    }
}