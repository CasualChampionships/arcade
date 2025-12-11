/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.flashback

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.SharedConstants
import net.minecraft.core.UUIDUtil
import java.util.*

public data class FlashbackMeta(
    val uuid: UUID = UUID.randomUUID(),
    val name: String = "Unnamed",
    val version: String = SharedConstants.getCurrentVersion().name(),
    val worldName: String = "World",
    val dataVersion: Int = SharedConstants.getCurrentVersion().dataVersion().version,
    val protocolVersion: Int = SharedConstants.getCurrentVersion().protocolVersion(),
    val totalTicks: Int = 0,
    val markers: Map<String, FlashbackMarker> = mapOf(),
    val chunks: Map<String, ChunkMeta> = mapOf()
) {
    public fun completeChunk(totalTicks: Int, chunkName: String, forceSnapshot: Boolean): FlashbackMeta {
        val duration = totalTicks - this.totalTicks
        val copy = LinkedHashMap(this.chunks)
        copy[chunkName] = ChunkMeta(duration, forceSnapshot)
        return this.copy(totalTicks = totalTicks, chunks = copy)
    }

    public data class ChunkMeta(
        val duration: Int,
        val forcePlaySnapshot: Boolean
    ) {
        public companion object {
            public val CODEC: Codec<ChunkMeta> = RecordCodecBuilder.create { instance ->
                instance.group(
                    Codec.INT.fieldOf("duration").forGetter(ChunkMeta::duration),
                    Codec.BOOL.fieldOf("forcePlaySnapshot").forGetter(ChunkMeta::forcePlaySnapshot)
                ).apply(instance, ::ChunkMeta)
            }
        }
    }

    public companion object {
        public val CODEC: Codec<FlashbackMeta> = RecordCodecBuilder.create { instance ->
            instance.group(
                UUIDUtil.STRING_CODEC.fieldOf("uuid").forGetter(FlashbackMeta::uuid),
                Codec.STRING.fieldOf("name").forGetter(FlashbackMeta::name),
                Codec.STRING.fieldOf("version_string").forGetter(FlashbackMeta::version),
                Codec.STRING.fieldOf("world_name").forGetter(FlashbackMeta::worldName),
                Codec.INT.fieldOf("data_version").forGetter(FlashbackMeta::dataVersion),
                Codec.INT.fieldOf("protocol_version").forGetter(FlashbackMeta::protocolVersion),
                Codec.INT.fieldOf("total_ticks").forGetter(FlashbackMeta::totalTicks),
                Codec.unboundedMap(Codec.STRING, FlashbackMarker.CODEC).fieldOf("markers").forGetter(FlashbackMeta::markers),
                Codec.unboundedMap(Codec.STRING, ChunkMeta.CODEC).fieldOf("chunks").forGetter(FlashbackMeta::chunks)
            ).apply(instance, ::FlashbackMeta)
        }
    }
}