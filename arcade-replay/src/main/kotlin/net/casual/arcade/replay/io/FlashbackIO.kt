/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io

import java.nio.file.Path
import kotlin.io.path.extension
import kotlin.io.path.isRegularFile

public object FlashbackIO {
    // Magic number flashback uses to verify that it's a flashback file
    public const val MAGIC_NUMBER: Int = -0x287F177C

    public const val CHUNK_LENGTH: Int = 5 * 60 * 20
    public const val LEVEL_CHUNK_CACHE_SIZE: Int = 10000

    public const val METADATA: String = "metadata.json"
    public const val METADATA_OLD: String = "$METADATA.old"
    public const val CHUNK_CACHES: String = "level_chunk_caches"

    public fun isFlashbackFile(path: Path): Boolean {
        return path.isRegularFile() && path.extension == "zip"
    }

    public fun getChunkCacheFileIndex(index: Int): Int {
        return index / LEVEL_CHUNK_CACHE_SIZE
    }
}