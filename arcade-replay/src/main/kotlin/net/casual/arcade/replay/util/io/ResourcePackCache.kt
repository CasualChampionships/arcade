/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.io

import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import net.casual.arcade.utils.ArcadeUtils
import java.net.URI
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.*

internal object ResourcePackCache {
    private val cached = ArcadeUtils.path.resolve("replays").resolve("packs")

    private val caching = ConcurrentHashMap<String, CompletableFuture<ByteArray>>()

    fun get(url: String): CompletableFuture<ByteArray> {
        val urlHash = this.sha1().hashString(url, StandardCharsets.UTF_8).toString()
        val caching = this.caching[urlHash]
        if (caching != null) {
            return caching
        }

        val cached = this.cached.resolve(urlHash)
        if (cached.exists()) {
            return CompletableFuture.completedFuture(cached.readBytes())
        }

        val future = CompletableFuture.supplyAsync {
            val bytes = URI(url).toURL().openStream().readAllBytes()
            cached.createParentDirectories().writeBytes(bytes)
            bytes
        }
        future.whenComplete { _, throwable ->
            this.caching.remove(urlHash)
            if (throwable != null) {
                ArcadeUtils.logger.error("Failed to download resource pack at $url", throwable)
            }
        }
        return future
    }

    fun hash(bytes: ByteArray): String {
        return this.sha1().hashBytes(bytes).toString()
    }

    @Suppress("DEPRECATION")
    private fun sha1(): HashFunction {
        return Hashing.sha1()
    }
}