/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.io

import net.casual.arcade.utils.ArcadeUtils
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.*

public object ReplayModIO {
    public fun isReplayFile(location: Path): Boolean {
        return location.isRegularFile() && location.extension == "mcpr"
    }

    public fun deleteCaches(location: Path) {
        try {
            val caches = location.parent.resolve(location.name + ".cache")
            if (caches.exists()) {
                @OptIn(ExperimentalPathApi::class)
                caches.deleteRecursively()
            }
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to delete caches", e)
        }
    }
}