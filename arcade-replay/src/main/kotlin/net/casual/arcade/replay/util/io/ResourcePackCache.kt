/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.replay.util.io

import com.google.common.hash.HashFunction
import com.google.common.hash.Hashing
import net.casual.arcade.utils.ArcadeUtils
import java.net.URI
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

internal object ResourcePackCache {
    private val cached = ArcadeUtils.path.resolve("replays").resolve("packs")

    private val caching = ConcurrentHashMap<String, CompletableFuture<ByteArray>>()

    fun get(url: String, hash: String): CompletableFuture<ByteArray> {
        if (hash.isEmpty()) {
            return this.download(url, hash)
        }

        val cached = this.cached.resolve(hash)
        if (cached.exists()) {
            return CompletableFuture.completedFuture(cached.readBytes())
        }

        val pending = CompletableFuture<ByteArray>()
        val downloading = this.caching.putIfAbsent(hash, pending)
        if (downloading != null) {
            return downloading
        }

        this.download(url, hash).whenComplete { bytes, throwable ->
            this.caching.remove(hash, pending)
            if (throwable != null) {
                pending.completeExceptionally(throwable)
            } else {
                pending.complete(bytes)
            }
        }
        return pending
    }

    private fun download(url: String, hash: String): CompletableFuture<ByteArray> {
        val future = CompletableFuture.supplyAsync {
            val bytes = URI(url).toURL().openStream().readAllBytes()
            if (hash.isNotEmpty() && hash == this.hash(bytes)) {
                this.cached.resolve(hash).createParentDirectories().writeBytes(bytes)
            }
            bytes
        }
        return future.whenComplete { _, throwable ->
            if (throwable != null) {
                ArcadeUtils.logger.error("Failed to download resource pack at $url", throwable)
            }
        }
    }

    private fun hash(bytes: ByteArray): String {
        return this.sha1().hashBytes(bytes).toString()
    }

    @Suppress("DEPRECATION")
    private fun sha1(): HashFunction {
        return Hashing.sha1()
    }
}