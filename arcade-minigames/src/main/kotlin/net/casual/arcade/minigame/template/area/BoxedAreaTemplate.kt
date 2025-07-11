/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.area

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.minigame.area.BoxedArea
import net.casual.arcade.minigame.area.PlaceableArea
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.casual.arcade.utils.encodedOptionalFieldOf
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

    override fun codec(): MapCodec<BoxedAreaTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<BoxedAreaTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("boxed")

        override val CODEC: MapCodec<BoxedAreaTemplate> = OrderedRecordCodecBuilder.mapCodec { instance ->
            instance.group(
                Vec3i.CODEC.encodedOptionalFieldOf("position", Vec3i.ZERO).forGetter(BoxedAreaTemplate::position),
                Codec.INT.encodedOptionalFieldOf("radius", 20).forGetter(BoxedAreaTemplate::radius),
                Codec.INT.encodedOptionalFieldOf("height", 5).forGetter(BoxedAreaTemplate::height),
                BuiltInRegistries.BLOCK.byNameCodec().encodedOptionalFieldOf("block", Blocks.BARRIER).forGetter(BoxedAreaTemplate::block),
            ).apply(instance, ::BoxedAreaTemplate)
        }
    }
}