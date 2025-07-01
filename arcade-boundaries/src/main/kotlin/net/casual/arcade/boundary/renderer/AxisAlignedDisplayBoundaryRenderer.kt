/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.boundary.renderer

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.ManualAttachment
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.casual.arcade.boundary.renderer.options.AxisAlignedModelRenderOptions
import net.casual.arcade.boundary.shape.BoundaryShape
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.PlayerUtils.isChunkInViewDistance
import net.casual.arcade.utils.codec.CodecProvider
import net.minecraft.core.Direction
import net.minecraft.core.SectionPos
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundChunksBiomesPacket
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.dimension.DimensionType
import java.util.*
import java.util.function.Consumer

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
 * @param models The models to use for the item displays.
 * @see AxisAlignedModelRenderOptions
 */
public class AxisAlignedDisplayBoundaryRenderer(
    private val shape: BoundaryShape,
    private val models: AxisAlignedModelRenderOptions = AxisAlignedModelRenderOptions.DEFAULT
): BoundaryRenderer {
    // The way that this renderer works relies on the fact that
    // Minecraft always renders entities outside world height.
    // So we shift all the display elements above the world
    // height then translate them back down so the player can see them.

    private val attachment: ManualAttachment
    private val faces = EnumUtils.mapOf<Direction, ItemDisplayElement>()

//    private val forceLoadingCenter = ObjectOpenHashSet<UUID>()

    // We use another hack, we need the display entity on the client
    // to be ticking so that the client can see the border updating.
    // ClientboundChunksBiomesPacket force-loads the chunk which
    // allows our display entity to tick!
    private lateinit var cached: ClientboundChunksBiomesPacket

    init {
        val holder = ElementHolder()
        this.createElements(holder, this.faces)

        this.attachment = ManualAttachment(holder, null) {
            this.shape.center().add(0.0, Y_SHIFT.toDouble(), 0.0)
        }
        this.updateFaces()
    }

    override fun render(level: ServerLevel, players: Collection<ServerPlayer>) {
        val shouldUpdateCenter = level.server.tickCount % 2 == 0
        if (shouldUpdateCenter) {
            val center = this.shape.center()
            val chunkX = SectionPos.blockToSectionCoord(center.x())
            val chunkZ = SectionPos.blockToSectionCoord(center.z())
            val packet = this.getOrCreateChunkPacket(level, chunkX, chunkZ)
            for (player in players) {
                if (!player.isChunkInViewDistance(chunkX, chunkZ)) {
                    player.connection.send(packet)
                }
            }
        }

        this.updateFaces()
    }

    override fun startRendering(player: ServerPlayer) {
        val center = this.shape.center()
        val chunkX = SectionPos.blockToSectionCoord(center.x())
        val chunkZ = SectionPos.blockToSectionCoord(center.z())
        if (!player.isChunkInViewDistance(chunkX, chunkZ)) {
//            this.forceLoadingCenter.add(player.uuid)
            val packet = this.getOrCreateChunkPacket(player.level(), chunkX, chunkZ)
            player.connection.send(packet)
        }

        this.attachment.startWatching(player)
    }

    override fun stopRendering(player: ServerPlayer) {
        this.attachment.stopWatching(player)
//        this.forceLoadingCenter.remove(player.uuid)
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

    private fun updateFaces() {
        for ((direction, element) in this.faces) {
            val (model, brightness) = this.models.get(this.shape, direction)
            // ItemStack#equals isn't implemented, so just setting it always marks it dirty
            if (!ItemStack.isSameItemSameComponents(model, element.item)) {
                element.item = model
            }
            element.brightness = brightness
            this.updateFace(direction, element)
        }
        this.attachment.tick()
    }

    private fun updateFace(direction: Direction, element: ItemDisplayElement) {
        val size = this.shape.size().toVector3f()
        val scale = direction.step().absolute().sub(1.0F, 1.0F, 1.0F).negate()
        element.scale = scale.mul(size)

        val translation = size.mul(direction.unitVec3f).mul(0.5F)
            .sub(0.0F, Y_SHIFT.toFloat(), 0.0F)

        val zFightingShift = direction.opposite.step().mul(0.01F)
        element.translation = translation.add(zFightingShift)
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
        override fun create(shape: BoundaryShape): BoundaryRenderer {
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
        private val Y_SHIFT = DimensionType.Y_SIZE
    }
}