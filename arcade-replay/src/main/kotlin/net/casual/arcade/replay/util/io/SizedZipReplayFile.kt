/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.io

import com.replaymod.replaystudio.replay.ZipReplayFile
import com.replaymod.replaystudio.studio.ReplayStudio
import org.apache.commons.lang3.mutable.MutableLong
import java.io.File
import java.io.OutputStream
import java.util.concurrent.ConcurrentHashMap

public class SizedZipReplayFile(
    input: File? = null,
    out: File,
    cache: File = File(out.parentFile, out.name + ".cache")
): ZipReplayFile(ReplayStudio(), input, out, cache) {
    private val entries = ConcurrentHashMap<String, MutableLong>()

    override fun write(entry: String): OutputStream {
        val mutable = MutableLong()
        this.entries[entry] = mutable
        return net.casual.arcade.replay.util.io.CounterOutputStream(super.write(entry), mutable)
    }

    public fun getRawFileSize(): Long {
        return this.entries.values.fold(0L) { a, l -> a + l.value }
    }
}