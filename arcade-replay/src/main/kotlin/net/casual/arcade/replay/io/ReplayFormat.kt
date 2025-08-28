/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io

import net.casual.arcade.replay.recorder.ReplayRecorder
import net.casual.arcade.replay.viewer.ReplayViewer
import net.casual.arcade.replay.io.reader.ReplayReader
import net.casual.arcade.replay.io.reader.flashback.FlashbackReader
import net.casual.arcade.replay.io.reader.replay_mod.ReplayModReader
import net.casual.arcade.replay.io.writer.ReplayWriter
import net.casual.arcade.replay.io.writer.flashback.FlashbackWriter
import net.casual.arcade.replay.io.writer.replay_mod.ReplayModWriter
import net.casual.arcade.utils.convertCasing
import net.casual.arcade.utils.string.PascalCase
import net.casual.arcade.utils.string.SnakeCase
import java.nio.file.Path

public enum class ReplayFormat(
    private val stable: Boolean,
    private val experimental: Boolean
) {
    ReplayMod(true, false),
    Flashback(true, true);

    public fun writer(recordings: Path): (ReplayRecorder) -> ReplayWriter {
        return when (this) {
            ReplayMod -> ReplayModWriter.dated(recordings)
            Flashback -> FlashbackWriter.dated(recordings)
        }
    }

    public fun reader(path: Path): (ReplayViewer) -> ReplayReader {
        return when (this) {
            ReplayMod -> ({ ReplayModReader(it, path) })
            Flashback -> ({ FlashbackReader(it, path)})
        }
    }

    public fun warn(consumer: (String) -> Unit) {
        if (this.experimental) {
            consumer.invoke("${this.name} support is currently experimental: you may encounter issues with your recordings, including issues that may cause recordings to be corrupt, you have been warned!")
            consumer.invoke("If you do encounter any issues please submit an issue report to https://github.com/senseiwells/ServerReplay/issues")
        }
        if (!this.stable) {
            consumer.invoke("${this.name} support is currently unstable: ${this.name} hasn't released yet, your replays may not be compatible in the future, you have been warned!")
        }
    }

    public fun id(): String {
        return this.name.convertCasing(PascalCase, SnakeCase)
    }

    public companion object {
        public fun formatOf(path: Path): ReplayFormat? {
            return when {
                ReplayModIO.isReplayFile(path) -> ReplayMod
                FlashbackIO.isFlashbackFile(path) -> Flashback
                else -> null
            }
        }
    }
}