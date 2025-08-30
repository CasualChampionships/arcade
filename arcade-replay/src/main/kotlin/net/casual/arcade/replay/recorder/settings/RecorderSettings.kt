/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.recorder.settings

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.replay.io.ReplayFormat
import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.util.io.FileSize
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.casual.arcade.utils.codec.OrderedRecordCodecBuilder
import net.casual.arcade.utils.convertCasing
import net.casual.arcade.utils.encodedOptionalFieldOf
import net.casual.arcade.utils.string.PascalCase
import net.casual.arcade.utils.string.SnakeCase
import net.minecraft.util.StringRepresentable
import kotlin.time.Duration

/**
 * Settings for the [ReplayRecorder].
 */
public interface RecorderSettings {
    /**
     * The debug flag, this enables some debug features
     * for the recorders, typically this should just be disabled.
     */
    public val debug: Boolean

    /**
     * This is the world name used in both flashback and
     * replay mod metadata.
     */
    public val worldName: String

    /**
     * This is the server name used in replay mod metadata.
     */
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
     * Specifies how a chunk recorder decides whether
     * to pause/resume the current recording.
     */
    public val chunkRecordingStrategy: ChunkRecordingStrategy

    /**
     * Configurations for file limitations.
     *
     * @see FileLimits
     */
    public val limits: FileLimits

    /**
     * Configurations for ignored packets.
     *
     * @see IgnorePackets
     */
    public val ignores: IgnorePackets

    /**
     * Configuration for packet optimizations.
     *
     * @see OptimizePackets
     */
    public val optimizes: OptimizePackets

    /**
     * Whether to record the player hotbar.
     */
    public val recordHotbar: Boolean

    /**
     * Whether to record voice chat, with simple voice chat, or not.
     */
    public val recordVoiceChat: Boolean

    /**
     * This enum represents how a chunk recorder should
     * record chunks over its lifetime.
     */
    public enum class ChunkRecordingStrategy: StringRepresentable {
        /**
         * This strategy indicates that the recorder should always
         * keep recording the chunks, even if they're completely
         * unloaded.
         *
         * In the replay, this will essentially just be reflected
         * as a static world, but other server events such as
         * chat messages and the time of day will be recorded.
         */
        Always,

        /**
         * This strategy indicates that the recorder should
         * only keep recording the chunks if any of the given
         * chunks in the area is loaded.
         *
         * If all chunks within the area are not loaded, then
         * the recorder will pause and only resume recording
         * when one of the chunks is loaded again, essentially
         * skipping forward in time in the replay.
         *
         * The implementation differs between [ReplayFormat.ReplayMod]
         * and [ReplayFormat.Flashback], that being that replay mod
         * will record any server events during the paused period
         * (such as chat messages), but flashback will *not*.
         */
        ChunkLoaded,

        /**
         * This strategy indicates that the recorder should only
         * keep recording the chunks if a player is within the
         * area.
         *
         * If there are no players within the chunk area, then
         * the recorder will pause and only resume recording
         * when a player enters the recording area again, essentially
         * skipping forward time in the replay.
         *
         * **This is only supported for [ReplayFormat.Flashback]**
         */
        ChunkContainsPlayer,

        /**
         * This strategy indicates that the recorder should only
         * keep recording the chunks if a player is within the
         * area.
         *
         * If there are no non-spectator players within the chunk
         * area, then the recorder will pause and only resume recording
         * when a non-spectator player is in the recording area again,
         * essentially skipping forward time in the replay.
         *
         * **This is only supported for [ReplayFormat.Flashback]**
         */
        ChunkContainsNonSpectatorPlayer;

        override fun getSerializedName(): String {
            return this.name.convertCasing(PascalCase, SnakeCase)
        }

        public companion object {
            public val CODEC: Codec<ChunkRecordingStrategy> = StringRepresentable.fromEnum(ChunkRecordingStrategy::values)
        }
    }

    public data class FileLimits(
        /**
         * The max raw size that is permitted for replay recordings.
         *
         * A file size with `0` bytes indicates no max size.
         */
        public val maxRawSize: FileSize = FileSize(0),
        /**
         * Whether to automatically boot up a new recorder
         * if the replay ended due to hitting the max size limit.
         */
        public val restartAfterMaxRawSize: Boolean = false,
        /**
         * The max duration that is permitted for replay recordings.
         *
         * A duration of `0` indicates no max duration.
         */
        public val maxDuration: Duration = Duration.ZERO,
        /**
         * Whether to automatically boot up a new recorder
         * if the replay ended due to hitting the max duration limit.
         */
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
        /**
         * Whether to ignore custom payload packets.
         *
         * This should only be enabled as a last resort to try
         * to resolve a mod conflict.
         */
        public val customPayloadPackets: Boolean = false,
        /**
         * Whether to ignore sound packets.
         */
        public val soundPackets: Boolean = false,
        /**
         * Whether to ignore light packets.
         */
        public val lightPackets: Boolean = true,
        /**
         * Whether to ignore chat packets.
         */
        public val chatPackets: Boolean = false,
        /**
         * Whether to ignore action bar packets.
         */
        public val actionBarPackets: Boolean = false,
        /**
         * Whether to ignore scoreboard packets.
         */
        public val scoreboardPackets: Boolean = false
    ) {
        public companion object {
            public val DEFAULT: IgnorePackets = IgnorePackets()

            public val MAP_CODEC: MapCodec<IgnorePackets> = OrderedRecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.BOOL.encodedOptionalFieldOf("custom_payload_packets", false).forGetter(IgnorePackets::customPayloadPackets),
                    Codec.BOOL.encodedOptionalFieldOf("sound_packets", false).forGetter(IgnorePackets::soundPackets),
                    Codec.BOOL.encodedOptionalFieldOf("light_packets", true).forGetter(IgnorePackets::lightPackets),
                    Codec.BOOL.encodedOptionalFieldOf("chat_packets", false).forGetter(IgnorePackets::chatPackets),
                    Codec.BOOL.encodedOptionalFieldOf("action_bar_packets", false).forGetter(IgnorePackets::actionBarPackets),
                    Codec.BOOL.encodedOptionalFieldOf("scoreboard_packets", false).forGetter(IgnorePackets::scoreboardPackets)
                ).apply(instance, ::IgnorePackets)
            }
        }
    }

    public data class OptimizePackets(
        /**
         * Whether to optimize explosion packets.
         *
         * *This currently is not implemented*
         */
        public val explosionPackets: Boolean = true,
        /**
         * Whether to optimize certain entity packets.
         *
         * This optimization involves ignoring certain
         * clientbound entity packets which the client
         * can predict and calculate by itself.
         */
        public val entityPackets: Boolean = false
    ) {
        public companion object {
            public val DEFAULT: OptimizePackets = OptimizePackets()

            public val MAP_CODEC: MapCodec<OptimizePackets> = OrderedRecordCodecBuilder.mapCodec { instance ->
                instance.group(
                    Codec.BOOL.encodedOptionalFieldOf("explosion_packets", true).forGetter(OptimizePackets::explosionPackets),
                    Codec.BOOL.encodedOptionalFieldOf("entity_packets", false).forGetter(OptimizePackets::entityPackets)
                ).apply(instance, ::OptimizePackets)
            }
        }
    }
}