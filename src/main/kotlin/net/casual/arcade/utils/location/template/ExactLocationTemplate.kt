package net.casual.arcade.utils.location.template

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.location.Location
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public class ExactLocationTemplate(
    public val position: Vec3 = Vec3.ZERO,
    public val rotation: Vec2 = Vec2.ZERO
): LocationTemplate {
    override fun get(level: ServerLevel): Location {
        return Location.of(this.position, this.rotation, level)
    }

    override fun codec(): MapCodec<out LocationTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<ExactLocationTemplate> {
        override val ID: ResourceLocation = Arcade.id("exact")

        override val CODEC: MapCodec<ExactLocationTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Vec3.CODEC.encodedOptionalFieldOf("position", Vec3.ZERO).forGetter(ExactLocationTemplate::position),
                ArcadeExtraCodecs.VEC2.encodedOptionalFieldOf("rotation", Vec2.ZERO).forGetter(ExactLocationTemplate::rotation)
            ).apply(instance, ::ExactLocationTemplate)
        }
    }
}