/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer

import com.mojang.serialization.MapCodec
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.border.shape.AxisAlignedBoundaryShape
import net.casual.arcade.border.shape.BoundaryShape
import net.casual.arcade.border.utils.BundlingElementHolder
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.core.Direction
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.util.Brightness
import net.minecraft.world.item.Items
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer

public class AxisAlignedDisplayBoundaryRenderer(
    private val shape: AxisAlignedBoundaryShape
): BoundaryRenderer {
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

    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory.INSTANCE
    }

    private fun createElements(holder: ElementHolder, map: EnumMap<Direction, ItemDisplayElement>) {
        for (direction in Direction.entries) {
            val element = ItemDisplayElement(Items.BLUE_STAINED_GLASS)
            element.brightness = Brightness.FULL_BRIGHT
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
        val scale = direction.step().absolute().sub(1.0f, 1.0f, 1.0f).negate()
        element.scale = scale.mul(size)

        val translation = size.mul(direction.step()).mul(0.5F)
        element.translation = translation.sub(0.0F, Y_SHIFT.toFloat(), 0.0F)
        element.startInterpolation()
    }

    public class Factory: BoundaryRenderer.Factory {
        override fun create(shape: BoundaryShape): BoundaryRenderer {
            require(shape is AxisAlignedBoundaryShape)
            return AxisAlignedDisplayBoundaryRenderer(shape)
        }

        override fun codec(): MapCodec<out BoundaryRenderer.Factory> {
            return CODEC
        }

        public companion object: CodecProvider<Factory> {
            public val INSTANCE: Factory = Factory()

            override val ID: ResourceLocation = ArcadeUtils.id("axis_aligned_display_boundary_renderer")

            override val CODEC: MapCodec<out Factory> = MapCodec.unit(INSTANCE)
        }
    }

    private companion object {
        private val Y_SHIFT = DimensionType.Y_SIZE
    }
}