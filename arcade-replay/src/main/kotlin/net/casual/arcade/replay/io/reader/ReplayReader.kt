/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io.reader

import com.google.common.collect.Multimap
import net.casual.arcade.replay.util.ReplayMarker
import java.io.InputStream
import java.nio.file.Path
import kotlin.time.Duration

public interface ReplayReader {
    public val duration: Duration
    public val path: Path

    public fun jumpTo(timestamp: Duration): Boolean

    public fun readPackets(): Sequence<ReplayPacketData>

    public fun readResourcePack(hash: String): InputStream?

    public fun readMarkers(): Multimap<String?, ReplayMarker>

    public fun close()
}