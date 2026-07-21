/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.renderer

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.boundary.extension.PlayerEntityTickingChunkTrackerExtension.Companion.entityTickingChunkTrackerExtension
import net.casual.arcade.boundary.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.observer.Observer
import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.utils.asPlayerOrNull
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.level.server
import net.casual.arcade.utils.network.PacketSender
import net.casual.arcade.utils.serialization.codec.CodecProvider
import net.casual.arcade.virtual.entity.attachment.SimpleVirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.attachment.anchor.AttachmentAnchor
import net.casual.arcade.virtual.entity.display.SimpleVirtualItemDisplay
import net.casual.arcade.virtual.entity.location.VirtualPosition
import net.casual.arcade.virtual.entity.utils.attachWithParentObservers
import net.casual.arcade.virtual.entity.utils.createVirtualEntityAttachment
import net.casual.arcade.virtual.entity.utils.removeVirtualEntityAttachment
import net.minecraft.core.Direction
import net.minecraft.core.SectionPos
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.dimension.DimensionType
import java.util.*

/**
 * Implementation of [BoundaryRenderer] that assumes the boundary
 * is axis aligned, and renders it as such using item display
 * elements.
 *
 * This renderer can be used to simulate an almost 1:1 version of
 * the vanilla world border, however, requires a resource pack.
 * See [AxisAlignedModelRenderOptions.CUBE_SHADER] and
 * [AxisAlignedModelRenderOptions.CUBOID_SHADER] for more information.
 *
 * @param shape The shape to render.
 * @param level The level that the boundary is for.
 * @param models The models to use for the item displays.
 * @see AxisAlignedModelRenderOptions
 */
public class AxisAlignedDisplayBoundaryRenderer(
    private val level: ServerLevel,
    private val shape: BoundaryShape,
    private val models: AxisAlignedModelRenderOptions = AxisAlignedModelRenderOptions.DEFAULT
): BoundaryRenderer {
    // The way that this renderer works relies on the fact that
    // Minecraft always renders entities outside world height.
    // So we shift all the display elements above the world
    // height then translate them back down so the player can see them.

    private val attachment = this.level.createVirtualEntityAttachment { anchor, observers ->
        Attachment(anchor, observers, this)
    }
    private val faces = EnumUtils.mapOf<Direction, SimpleVirtualItemDisplay>()

    // We use another hack, we need the display entity on the client
    // to be ticking so that the client can see the border updating.
    // ClientboundChunksBiomesPacket force-loads the chunk which
    // allows our display entity to tick!
    private lateinit var cached: ClientboundChunksBiomesPacket

    init {
        this.createElements(this.faces)

        this.updateFaces()
    }

    override fun render() {
        val shouldUpdateCenter = this.level.server().tickCount % 2 == 0
        if (shouldUpdateCenter) {
            val center = this.shape.center()
            val chunkX = SectionPos.blockToSectionCoord(center.x())
            val chunkZ = SectionPos.blockToSectionCoord(center.z())
            val packet = this.getOrCreateChunkPacket(level, chunkX, chunkZ)
            for (observer in this.attachment.observers) {
                val player = observer.asPlayerOrNull()
                if (player == null || !player.entityTickingChunkTrackerExtension.isLoaded(chunkX, chunkZ)) {
                    observer.send(packet)
                }
            }
        }

        this.updateFaces()
    }

    override fun stop() {
        this.level.removeVirtualEntityAttachment(this.attachment)
    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory(this.models)
    }

    private fun createElements(map: EnumMap<Direction, SimpleVirtualItemDisplay>) {
        for (direction in Direction.entries) {
            val (stack, brightness) = this.models.get(this.shape, direction)
            val entity = this.attachment.attachWithParentObservers(::BoundaryVirtualItemDisplay)
            entity.setItemStack(stack)
            entity.setBrightness(brightness)
            entity.setInvisible(true)
            entity.setViewRange(Y_SHIFT.toFloat())
            entity.setTeleportationInterpolation(1)
            entity.setTransformationInterpolation(1)
            map[direction] = entity
        }
    }

    private fun updateFaces() {
        for ((direction, entity) in this.faces) {
            val (model, brightness) = this.models.get(this.shape, direction)
            entity.setItemStack(model)
            entity.setBrightness(brightness)
            this.updateFace(direction, entity)
        }
        this.attachment.tick()
    }

    private fun updateFace(direction: Direction, element: SimpleVirtualItemDisplay) {
        val size = this.shape.size().toVector3f()
        val scale = direction.step().absolute().sub(1.0F, 1.0F, 1.0F).negate()
        element.setScale(scale.mul(size))

        element.position = VirtualPosition.Absolute(this.shape.center().add(0.0, Y_SHIFT.toDouble(), 0.0))
        val translation = size.mul(direction.unitVec3f).mul(0.5F)
            .sub(0.0F, Y_SHIFT.toFloat(), 0.0F)
        val zFightingShift = direction.opposite.step().mul(0.01F)
        element.setTranslation(translation.add(zFightingShift))
        element.startInterpolationIfDirty()
    }

    private fun getOrCreateChunkPacket(level: ServerLevel, chunkX: Int, chunkZ: Int): ClientboundChunksBiomesPacket {
        if (this::cached.isInitialized) {
            val pos = this.cached.chunkBiomeData[0].pos
            if (pos.x == chunkX && pos.z == chunkZ) {
                return this.cached
            }
        }
        val chunk = level.getChunk(chunkX, chunkZ)
        val packet = ClientboundChunksBiomesPacket.forChunks(listOf(chunk))
        this.cached = packet
        return packet
    }

    public class Factory(
        private val models: AxisAlignedModelRenderOptions
    ): BoundaryRenderer.Factory {
        override fun create(level: ServerLevel, shape: BoundaryShape): BoundaryRenderer {
            return AxisAlignedDisplayBoundaryRenderer(level, shape, this.models)
        }

        override fun codec(): MapCodec<out BoundaryRenderer.Factory> {
            return codec
        }

        public companion object: CodecProvider<Factory> {
            override val id: Identifier = arcade("axis_aligned_display_boundary_renderer")

            override val codec: MapCodec<out Factory> = RecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    AxisAlignedModelRenderOptions.CODEC.fieldOf("models").forGetter(Factory::models)
                ).apply(instance, ::Factory)
            }
        }
    }

    private class BoundaryVirtualItemDisplay(
        attachment: VirtualEntityAttachment,
        observers: ObserverTracker
    ): SimpleVirtualItemDisplay(attachment, observers) {
        override fun canObserve(observer: Observer): Boolean {
            return true
        }
    }

    private class Attachment(
        anchor: AttachmentAnchor,
        observers: ObserverTracker,
        private val renderer: AxisAlignedDisplayBoundaryRenderer
    ): SimpleVirtualEntityAttachment(anchor, observers) {
        override fun sendRootSpawnPackets(observer: Observer, sender: PacketSender) {
            val center = this.renderer.shape.center()
            val chunkX = SectionPos.blockToSectionCoord(center.x())
            val chunkZ = SectionPos.blockToSectionCoord(center.z())
            val player = observer.asPlayerOrNull()
            if (player == null || !player.entityTickingChunkTrackerExtension.isLoaded(chunkX, chunkZ)) {
                val packet = this.renderer.getOrCreateChunkPacket(observer.location.level, chunkX, chunkZ)
                sender.send(packet)
            }
        }
    }

    private companion object {
        private val Y_SHIFT = DimensionType.Y_SIZE
    }
}