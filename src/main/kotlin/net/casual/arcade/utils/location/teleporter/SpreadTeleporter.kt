package net.casual.arcade.utils.location.teleporter

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.gui.shapes.ShapePoints
import net.casual.arcade.gui.shapes.Regular2DPolygonShape
import net.casual.arcade.utils.location.Location
import net.casual.arcade.utils.serialization.CodecProvider
import net.minecraft.commands.arguments.EntityAnchorArgument
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3

public class SpreadTeleporter(
    private val center: Vec3,
    private val radius: Double
): ShapedTeleporter() {
    override fun createShape(level: ServerLevel, points: Int): ShapePoints {
        return Regular2DPolygonShape.createHorizontal(this.center, this.radius, points)
    }

    override fun teleportEntity(entity: Entity, location: Location) {
        super.teleportEntity(entity, location)
        entity.lookAt(EntityAnchorArgument.Anchor.EYES, this.center)
    }

    override fun codec(): MapCodec<out EntityTeleporter> {
        return CODEC
    }

    public companion object: CodecProvider<SpreadTeleporter> {
        override val ID: ResourceLocation = Arcade.id("spread")

        override val CODEC: MapCodec<out SpreadTeleporter> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Vec3.CODEC.fieldOf("center").forGetter(SpreadTeleporter::center),
                Codec.DOUBLE.fieldOf("radius").forGetter(SpreadTeleporter::radius)
            ).apply(instance, ::SpreadTeleporter)
        }
    }
}