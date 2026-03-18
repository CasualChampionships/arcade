/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.teleporter

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.math.location.LocationWithLevel
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.visuals.shapes.ShapePoints.Companion.points
import net.casual.arcade.visuals.shapes.impl.RegularPolygonShape
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

public class SpreadTeleporter(
    private val center: Vec3,
    private val radius: Double
): ShapedTeleporter() {
    override fun createShape(level: ServerLevel, points: Int): Iterator<Vec3> {
        return RegularPolygonShape.createHorizontal(this.center, this.radius, points).points()
    }

    override fun teleportEntity(entity: Entity, location: LocationWithLevel<ServerLevel>) {
        super.teleportEntity(entity, location)
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, this.center)
    }

    override fun codec(): MapCodec<out EntityTeleporter> {
        return codec
    }

    public companion object: CodecProvider<SpreadTeleporter> {
        override val id: Identifier = IdentifierUtils.arcade("spread")

        override val codec: MapCodec<out SpreadTeleporter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Vec3.CODEC.fieldOf("center").forGetter(SpreadTeleporter::center),
                Codec.DOUBLE.fieldOf("radius").forGetter(SpreadTeleporter::radius)
            ).apply(instance, ::SpreadTeleporter)
        }
    }
}