/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement
import net.casual.arcade.border.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.border.shape.AxisAlignedBoundaryShape
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Brightness
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.dimension.DimensionType
import java.util.*
import java.util.function.Consumer

public class AxisAlignedDisplayBoundaryRenderer(
    private val shape: AxisAlignedBoundaryShape,
    private val models: AxisAlignedModelRenderOptions = AxisAlignedModelRenderOptions.DEFAULT
): BoundaryRenderer {
    // The way that this renderer works relies on the fact that
    // Minecraft always renders entities outside world height.
    // So we shift all the display elements above the world
    // height then translate them back down so the player can see them.

    private val attachment: ManualAttachment
    private val faces = EnumUtils.mapOf<Direction, ItemDisplayElement>()

    init {
        val holder = ElementHolder()
        this.createElements(holder, this.faces)

        this.attachment = ManualAttachment(holder, null) {
            this.shape.center().add(0.0, Y_SHIFT.toDouble(), 0.0)
        }
    }

    override fun render(players: Collection<ServerPlayer>) {
        for ((direction, element) in this.faces) {
            val (model, brightness) = this.models.get(this.shape, direction)
            element.item = model
            element.brightness = brightness
            this.updateFace(direction, element)
        }
        this.attachment.tick()
    }

    override fun startRendering(player: ServerPlayer) {
        this.attachment.startWatching(player)
    }

    override fun stopRendering(player: ServerPlayer) {
        this.attachment.stopWatching(player)
    }

    override fun restartRendering(
        player: ServerPlayer,
        sender: Consumer<Packet<ClientGamePacketListener>>
    ) {
        for (element in this.faces.values) {
            element.startWatching(player, sender)
        }
    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory(this.models)
    }

    private fun createElements(holder: ElementHolder, map: EnumMap<Direction, ItemDisplayElement>) {
        for (direction in Direction.entries) {
            val (stack, brightness) = this.models.get(this.shape, direction)
            val element = ItemDisplayElement(stack)
            element.brightness = brightness
            element.isInvisible = true
            element.viewRange = Y_SHIFT.toFloat()
            element.teleportDuration = 1
            element.interpolationDuration = 1
            map[direction] = element
            holder.addElement(element)
        }
    }

    private fun updateFace(direction: Direction, element: ItemDisplayElement) {
        val size = this.shape.size().toVector3f()
        val scale = direction.step().absolute().sub(1.0F, 1.0F, 1.0F).negate()
        element.scale = scale.mul(size)

        val translation = size.mul(direction.unitVec3f).mul(Z_FIGHTING_SCALE).mul(0.5F)
        element.translation = translation.sub(0.0F, Y_SHIFT.toFloat(), 0.0F)
        element.startInterpolationIfDirty()
    }

    public class Factory(
        private val models: AxisAlignedModelRenderOptions
    ): BoundaryRenderer.Factory {
        override fun create(shape: BoundaryShape): BoundaryRenderer {
            require(shape is AxisAlignedBoundaryShape)
            return AxisAlignedDisplayBoundaryRenderer(shape, this.models)
        }

        override fun codec(): MapCodec<out BoundaryRenderer.Factory> {
            return CODEC
        }

        public companion object: CodecProvider<Factory> {
            override val ID: ResourceLocation = ArcadeUtils.id("axis_aligned_display_boundary_renderer")

            override val CODEC: MapCodec<out Factory> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    AxisAlignedModelRenderOptions.CODEC.fieldOf("models").forGetter(Factory::models)
                ).apply(instance, ::Factory)
            }
        }
    }

    private companion object {
        private const val Z_FIGHTING_SCALE = 0.9999F
        private val Y_SHIFT = DimensionType.Y_SIZE
    }
}