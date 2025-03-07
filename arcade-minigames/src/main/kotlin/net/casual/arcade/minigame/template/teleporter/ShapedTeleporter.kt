/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.teleporter

import com.google.common.collect.Multimap
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.math.location.LocationWithLevel.Companion.asLocation
import net.casual.arcade.utils.teleportTo
import net.casual.arcade.visuals.shapes.ShapePoints
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public abstract class ShapedTeleporter: EntityTeleporter {
    protected abstract fun createShape(level: ServerLevel, points: Int): ShapePoints

    protected open fun teleportEntity(entity: Entity, location: LocationWithLevel<ServerLevel>) {
        entity.teleportTo(location)
    }

    protected open fun teleportTeam(team: PlayerTeam, entities: Collection<Entity>, location: LocationWithLevel<ServerLevel>) {
        for (entity in entities) {
            this.teleportEntity(entity, location)
        }
    }

    override fun teleportEntities(level: ServerLevel, entities: List<Entity>) {
        val shape = this.createShape(level, entities.size)
        for ((i, position) in shape.withIndex()) {
            this.teleportEntity(entities[i], level.asLocation(position))
        }
    }

    override fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>) {
        val shape = this.createShape(level, teams.keySet().size)
        for ((team, position) in teams.keySet().zip(shape)) {
            this.teleportTeam(team, teams[team], level.asLocation(position))
        }
    }
}