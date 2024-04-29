package net.casual.arcade.area.templates

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.Arcade
import net.casual.arcade.area.BoxedArea
import net.casual.arcade.area.PlaceableArea
import net.casual.arcade.utils.CodecUtils.encodedOptionalFieldOf
import net.casual.arcade.utils.serialization.CodecProvider
import net.minecraft.core.Vec3i
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.block.Block
import net.minecraft.world.level.block.Blocks

public class BoxedAreaTemplate(
    private val position: Vec3i = Vec3i.ZERO,
    private val radius: Int = 20,
    private val height: Int = 5,
    private val block: Block = Blocks.BARRIER
): PlaceableAreaTemplate {
    override fun create(level: ServerLevel): PlaceableArea {
        return BoxedArea(this.position, this.radius, this.height, level, this.block)
    }

    override fun codec(): Codec<BoxedAreaTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<BoxedAreaTemplate> {
        override val ID: ResourceLocation = Arcade.id("boxed")

        override val CODEC: Codec<BoxedAreaTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                Vec3i.CODEC.encodedOptionalFieldOf("position", Vec3i.ZERO).forGetter(BoxedAreaTemplate::position),
                Codec.INT.encodedOptionalFieldOf("radius", 20).forGetter(BoxedAreaTemplate::radius),
                Codec.INT.encodedOptionalFieldOf("height", 5).forGetter(BoxedAreaTemplate::height),
                BuiltInRegistries.BLOCK.byNameCodec().encodedOptionalFieldOf("block", Blocks.BARRIER).forGetter(BoxedAreaTemplate::block),
            ).apply(instance, ::BoxedAreaTemplate)
        }
    }
}