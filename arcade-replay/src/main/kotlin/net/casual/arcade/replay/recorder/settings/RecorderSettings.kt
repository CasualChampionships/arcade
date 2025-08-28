/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.settings

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.replay.util.io.FileSize
import net.casual.arcade.utils.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.casual.arcade.utils.encodedOptionalFieldOf
import kotlin.time.Duration

public interface RecorderSettings {
    public val debug: Boolean

    public val worldName: String

    public val serverName: String

    /**
     * Fixes the time of day during the replay to the
     * specified value (in game ticks), set to a
     * negative value to disable.
     *
     * See [the wiki](https://minecraft.wiki/w/Daylight_cycle#24-hour_Minecraft_day)
     * for further details about ticks and the daylight cycle.
     */
    public val fixedDaylightCycle: Long

    /**
     * Whether to record copies of the resource pack in
     * the replay file.
     *
     * This will slightly increase the replay file size,
     * but guarantees that the resource pack will be
     * available during viewing.
     *
     * **The Flashback format does not support this.**
     */
    public val includeResourcePacks: Boolean

    /**
     * The initial radius to load for chunk recordings.
     * This will load the chunks into memory in order to
     * record an initial snapshot of them, they do not
     * persist in memory, unless loaded in-game.
     *
     * If set to a *negative* value **all** chunks in the
     * chunk recorder will be loaded initially.
     *
     * The purpose of this setting is to allow for large
     * areas of chunks to be recorded without unnecessarily
     * loading all the chunks during the initial snapshot.
     */
    public val chunkRecorderLoadRadius: Int

    /**
     * Enabling this will automatically pause chunk
     * recordings when *all* chunks being recorded
     * are unloaded.
     */
    public val pauseWhenChunksUnloaded: Boolean

    public val limits: FileLimits

    public val ignores: IgnorePackets

    public val optimizes: OptimizePackets

    public val recordVoiceChat: Boolean

    public data class FileLimits(
        public val maxRawSize: FileSize = FileSize(0),
        public val restartAfterMaxRawSize: Boolean = false,
        public val maxDuration: Duration = Duration.ZERO,
        public val restartAfterMaxDuration: Boolean = false
    ) {
        public companion object {
            public val DEFAULT: FileLimits = FileLimits()

            public val MAP_CODEC: MapCodec<FileLimits> = OrderedRecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    FileSize.CODEC.fieldOf("max_raw_recording_file_size").forGetter(FileLimits::maxRawSize),
                    Codec.BOOL.fieldOf("restart_after_max_raw_recording_file_size").forGetter(FileLimits::restartAfterMaxRawSize),
                    ArcadeExtraCodecs.DURATION.fieldOf("max_recording_duration").forGetter(FileLimits::maxDuration),
                    Codec.BOOL.fieldOf("restart_after_max_recording_duration").forGetter(FileLimits::restartAfterMaxDuration)
                ).apply(instance, ::FileLimits)
            }
        }
    }

    public data class IgnorePackets(
        public val customPayloadPackets: Boolean = false,
        public val soundPackets: Boolean = false,
        public val lightPackets: Boolean = false,
        public val chatPackets: Boolean = false,
        public val actionBarPackets: Boolean = false,
        public val scoreboardPackets: Boolean = false
    ) {
        public companion object {
            public val DEFAULT: IgnorePackets = IgnorePackets()

            public val MAP_CODEC: MapCodec<IgnorePackets> = OrderedRecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.BOOL.encodedOptionalFieldOf("custom_payload_packets", false).forGetter(IgnorePackets::customPayloadPackets),
                    Codec.BOOL.encodedOptionalFieldOf("sound_packets", false).forGetter(IgnorePackets::soundPackets),
                    Codec.BOOL.encodedOptionalFieldOf("light_packets", false).forGetter(IgnorePackets::lightPackets),
                    Codec.BOOL.encodedOptionalFieldOf("chat_packets", false).forGetter(IgnorePackets::chatPackets),
                    Codec.BOOL.encodedOptionalFieldOf("action_bar_packets", false).forGetter(IgnorePackets::actionBarPackets),
                    Codec.BOOL.encodedOptionalFieldOf("scoreboard_packets", false).forGetter(IgnorePackets::scoreboardPackets)
                ).apply(instance, ::IgnorePackets)
            }
        }
    }

    public data class OptimizePackets(
        public val explosionPackets: Boolean = true,
        public val entityPackets: Boolean = false
    ) {
        public companion object {
            public val DEFAULT: OptimizePackets = OptimizePackets()

            public val MAP_CODEC: MapCodec<OptimizePackets> = OrderedRecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.BOOL.encodedOptionalFieldOf("explosion_packets", false).forGetter(OptimizePackets::explosionPackets),
                    Codec.BOOL.encodedOptionalFieldOf("entity_packets", false).forGetter(OptimizePackets::entityPackets)
                ).apply(instance, ::OptimizePackets)
            }
        }
    }
}