/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.border.renderer

import com.mojang.serialization.MapCodec
import eu.pb4.polymer.virtualentity.api.ElementHolder
import eu.pb4.polymer.virtualentity.api.attachment.HolderAttachment
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
import net.minecraft.world.item.Items
import net.minecraft.world.phys.Vec3
import java.util.*
import java.util.function.Consumer

public class AxisAlignedDisplayBoundaryRenderer(
    private val shape: AxisAlignedBoundaryShape
): BoundaryRenderer {
    private val players = Reference2ObjectOpenHashMap<ServerGamePacketListenerImpl, BorderAttachment>()

    override fun render(players: Collection<ServerPlayer>) {
        for (player in players) {
            val attachment = this.players[player.connection] ?: continue
            val anchor = getAnchorPosition(player)
            for ((direction, element) in attachment.faces) {
                this.updateFace(anchor, direction, element)
            }
            attachment.tick()
        }
    }

    override fun startRendering(player: ServerPlayer) {
        val connection = player.connection
        val holder = BundlingElementHolder()
        val faces = EnumUtils.mapOf<Direction, ItemDisplayElement>()
        for (direction in Direction.entries) {
            val element = ItemDisplayElement(Items.BLUE_STAINED_GLASS)
            faces[direction] = element
            holder.addElement(element)
        }
        val attachment = BorderAttachment(faces, holder, connection)
        this.players[connection] = attachment
        attachment.startWatching(connection)
    }

    override fun stopRendering(player: ServerPlayer) {
        this.players[player.connection]?.destroy()
    }

    override fun restartRendering(
        player: ServerPlayer,
        sender: Consumer<Packet<ClientGamePacketListener>>
    ) {

    }

    override fun factory(): BoundaryRenderer.Factory {
        return Factory.INSTANCE
    }

    private fun updateFace(anchor: Vec3, direction: Direction, element: ItemDisplayElement) {
        val size = this.shape.size().toVector3f()
        val scale = direction.step().absolute().sub(1.0f, 1.0f, 1.0f).negate()
        element.scale = scale.mul(size)

        val center = this.shape.center().toVector3f()
        val offset = direction.step().mul(size).mul(0.5F)
        val faceCenter = center.add(offset)

        element.translation = faceCenter.sub(anchor.toVector3f())
    }

    private class BorderAttachment(
        val faces: EnumMap<Direction, ItemDisplayElement>,
        private val holder: ElementHolder,
        private val owner: ServerGamePacketListenerImpl
    ): HolderAttachment {
        val player: ServerPlayer get() = this.owner.player

        init {
            this.holder.attachment = this
        }

        override fun holder(): ElementHolder {
            return this.holder
        }

        override fun destroy() {
            if (this.holder.attachment == this) {
                this.holder.attachment = null
                this.holder.destroy()
            }
        }

        override fun getPos(): Vec3 {
            return getAnchorPosition(this.player)
        }

        override fun getWorld(): ServerLevel {
            return this.player.level()
        }

        override fun updateCurrentlyTracking(tracking: Collection<ServerGamePacketListenerImpl>) {

        }

        override fun updateTracking(tracking: ServerGamePacketListenerImpl) {

        }
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
        fun getAnchorPosition(player: ServerPlayer): Vec3 {
            val x = player.chunkPosition().minBlockX.toDouble()
            val y = player.level().minY + player.level().height / 2.0
            val z = player.chunkPosition().minBlockZ.toDouble()
            return Vec3(x, y, z)
        }
    }
}