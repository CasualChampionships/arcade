package net.casual.arcade.utils.location.teleporter

import com.google.common.collect.Multimap
import net.casual.arcade.gui.shapes.ShapePoints
import net.casual.arcade.utils.EntityUtils.teleportTo
import net.casual.arcade.utils.location.Location
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.scores.PlayerTeam

public abstract class ShapedTeleporter: EntityTeleporter {
    protected abstract fun createShape(level: ServerLevel, points: Int): ShapePoints

    protected open fun teleportEntity(entity: Entity, location: Location) {
        entity.teleportTo(location)
    }

    override fun teleportEntities(level: ServerLevel, entities: List<Entity>) {
        val shape = this.createShape(level, entities.size)
        for ((i, position) in shape.withIndex()) {
            this.teleportEntity(entities[i], Location.of(position, level = level))
        }
    }

    override fun teleportTeams(level: ServerLevel, teams: Multimap<PlayerTeam, Entity>) {
        val shape = this.createShape(level, teams.keySet().size)
        for ((team, position) in teams.keys().zip(shape)) {
            for (entity in teams[team]) {
                this.teleportEntity(entity, Location.of(position, level = level))
            }
        }
    }
}