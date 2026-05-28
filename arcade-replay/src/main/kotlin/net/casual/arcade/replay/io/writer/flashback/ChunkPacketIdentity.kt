/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.writer.flashback

import net.casual.arcade.replay.mixins.flashback.BlockEntityInfoAccessor
import net.casual.arcade.replay.mixins.flashback.ClientboundLevelChunkPacketDataAccessor
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.network.protocol.game.ClientboundLevelChunkWithLightPacket

public class ChunkPacketIdentity private constructor(
    public val hashes: IntArray
) {
    public val hash: Int = this.hashes.contentHashCode()

    override fun hashCode(): Int {
        return this.hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other !is ChunkPacketIdentity) {
            return false
        }
        if (this.hash != other.hash) {
            return false
        }
        return this.hashes.contentEquals(other.hashes)
    }

    public companion object {
        public fun of(packet: ClientboundLevelChunkWithLightPacket): ChunkPacketIdentity {
            val hashes = IntArray(5)
            hashes[0] = packet.x
            hashes[1] = packet.z
            val chunkData = packet.chunkData as ClientboundLevelChunkPacketDataAccessor
            hashes[2] = chunkData.arcade_getBuffer().contentHashCode()
            hashes[3] = packet.chunkData.heightmaps.hashCode()
            val blockEntityHashes = IntArray(chunkData.arcade_getBlockEntitiesData().size)
            val sortedBlockEntityData = chunkData.arcade_getBlockEntitiesData().sortedBy { data ->
                data as BlockEntityInfoAccessor
                (data.arcade_getY() shl 8) or (data.arcade_getPackedXZ())
            }
            for ((i, data) in sortedBlockEntityData.withIndex()) {
                data as BlockEntityInfoAccessor
                val intermediary = intArrayOf(
                    data.arcade_getPackedXZ(),
                    data.arcade_getY(),
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.getId(data.arcade_getType()),
                    data.arcade_getTag()?.hashCode() ?: 0
                )
                blockEntityHashes[i] = intermediary.contentHashCode()
            }
            hashes[4] = blockEntityHashes.contentHashCode()

            return ChunkPacketIdentity(hashes)
        }
    }
}