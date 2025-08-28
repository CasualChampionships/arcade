/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.io

import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import net.casual.arcade.replay.util.FileUtils

public class FileSize(public val bytes: Long) {
    public fun formatted(): String {
        return FileUtils.formatSize(this.bytes)
    }

    override fun toString(): String {
        return this.formatted()
    }

    public companion object {
        public val STRING_CODEC: Codec<FileSize> = Codec.STRING.comapFlatMap(::parse, FileSize::formatted)
        public val LONG_CODEC: Codec<FileSize> = Codec.LONG.xmap(::FileSize, FileSize::bytes)

        public val CODEC: Codec<FileSize> = Codec.withAlternative(LONG_CODEC, STRING_CODEC)

        public fun parse(string: String): DataResult<FileSize> {
            val bytes = FileUtils.parseSize(string) ?: return DataResult.error { "$string is not a valid file size" }
            return DataResult.success(FileSize(bytes))
        }
    }
}