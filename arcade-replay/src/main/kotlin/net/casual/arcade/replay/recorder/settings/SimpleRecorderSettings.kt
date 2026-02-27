/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.settings

import com.google.gson.JsonElement
import com.mojang.serialization.Codec
import com.mojang.serialization.JsonOps
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.replay.recorder.settings.RecorderSettings.*
import net.casual.arcade.utils.serialization.codec.encodedOptionalFieldOf

public data class SimpleRecorderSettings(
    override val debug: Boolean = false,
    override val worldName: String = "World",
    override val serverName: String = "Server",
    override val fixedDaylightCycle: Long = -1,
    override val includeResourcePacks: Boolean = true,
    override val chunkRecorderLoadRadius: Int = -1,
    override val chunkRecordingStrategy: ChunkRecordingStrategy = ChunkRecordingStrategy.Always,
    override val limits: FileLimits = FileLimits.DEFAULT,
    override val ignores: IgnorePackets = IgnorePackets.DEFAULT,
    override val optimizes: OptimizePackets = OptimizePackets.DEFAULT,
    override val recordHotbar: Boolean = false,
    override val recordVoiceChat: Boolean = false,
    override val compressVoiceChatData: Boolean = false,
): RecorderSettings {
    public fun asJson(): JsonElement {
        return CODEC.encodeStart(JsonOps.INSTANCE, this).orThrow
    }

    public companion object {
        public val DEFAULT: SimpleRecorderSettings = SimpleRecorderSettings()

        public val CODEC: Codec<SimpleRecorderSettings> = RecordCodecBuilder.create { instance ->
            instance.group(
                Codec.BOOL.encodedOptionalFieldOf("debug", false).forGetter(RecorderSettings::debug),
                Codec.STRING.encodedOptionalFieldOf("world_name", "World").forGetter(RecorderSettings::worldName),
                Codec.STRING.encodedOptionalFieldOf("server_name", "Server").forGetter(RecorderSettings::serverName),
                Codec.LONG.encodedOptionalFieldOf("fixed_daylight_cycle", -1L).forGetter(RecorderSettings::fixedDaylightCycle),
                Codec.BOOL.encodedOptionalFieldOf("include_resource_packs", true).forGetter(RecorderSettings::includeResourcePacks),
                Codec.INT.encodedOptionalFieldOf("chunk_recorder_load_radius", -1).forGetter(RecorderSettings::chunkRecorderLoadRadius),
                ChunkRecordingStrategy.CODEC.encodedOptionalFieldOf("chunks_recorder_strategy", ChunkRecordingStrategy.Always).forGetter(RecorderSettings::chunkRecordingStrategy),
                FileLimits.MAP_CODEC.orElse(FileLimits.DEFAULT).forGetter(RecorderSettings::limits),
                IgnorePackets.MAP_CODEC.fieldOf("ignored_packets").orElse(IgnorePackets.DEFAULT).forGetter(RecorderSettings::ignores),
                OptimizePackets.MAP_CODEC.fieldOf("optimized_packets").orElse(OptimizePackets.DEFAULT).forGetter(RecorderSettings::optimizes),
                Codec.BOOL.encodedOptionalFieldOf("record_hotbar", false).forGetter(RecorderSettings::recordHotbar),
                Codec.BOOL.encodedOptionalFieldOf("record_voice_chat", false).forGetter(RecorderSettings::recordVoiceChat),
                Codec.BOOL.encodedOptionalFieldOf("compress_voice_chat_data", false).forGetter(RecorderSettings::compressVoiceChatData),
            ).apply(instance, ::SimpleRecorderSettings)
        }

        public fun RecorderSettings.asSimple(): SimpleRecorderSettings {
            if (this is SimpleRecorderSettings) {
                return this
            }
            return SimpleRecorderSettings(
                this.debug,
                this.worldName,
                this.serverName,
                this.fixedDaylightCycle,
                this.includeResourcePacks,
                this.chunkRecorderLoadRadius,
                this.chunkRecordingStrategy,
                this.limits,
                this.ignores,
                this.optimizes,
                this.recordHotbar,
                this.recordVoiceChat,
                this.compressVoiceChatData,
            )
        }
    }
}
