/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.area

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.minigame.area.PlaceableArea
import net.casual.arcade.minigame.area.StructureArea
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.StructureUtils
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.core.Vec3i
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.absolutePathString

public class StructuredAreaTemplate(
    private val path: Path,
    private val position: Vec3i,
): PlaceableAreaTemplate {
    private val structure by lazy {
        try {
            StructureUtils.read(this.path)
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to read structured area config path ${this.path.absolutePathString()}")
            null
        }
    }

    override fun create(level: ServerLevel): PlaceableArea {
        val structure = this.structure ?: return PlaceableAreaTemplate.DEFAULT.create(level)
        return StructureArea(structure, this.position, level)
    }

    override fun codec(): MapCodec<out PlaceableAreaTemplate> {
        return CODEC
    }

    public companion object: CodecProvider<StructuredAreaTemplate> {
        override val ID: ResourceLocation = ResourceUtils.arcade("structured")

        override val CODEC: MapCodec<StructuredAreaTemplate> = RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                ArcadeExtraCodecs.PATH.fieldOf("path").forGetter(StructuredAreaTemplate::path),
                Vec3i.CODEC.optionalFieldOf("position", Vec3i.ZERO).forGetter(StructuredAreaTemplate::position)
            ).apply(instance, ::StructuredAreaTemplate)
        }
    }
}