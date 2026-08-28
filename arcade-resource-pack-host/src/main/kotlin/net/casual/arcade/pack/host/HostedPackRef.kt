/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.host

import kotlinx.coroutines.future.await
import java.util.UUID
import java.util.concurrent.CompletableFuture

/**
 * A reference to a pack that has been added to a [PackHost].
 *
 * @param uuid The unique id identifying the pack on the host.
 * @see PackHost.add
 */
public class HostedPackRef internal constructor(
    /**
     * The unique id identifying the pack on the host.
     */
    public val uuid: UUID
) {
    private val future = CompletableFuture<HostedPack>()

    /**
     * Checks whether the pack has finished being hosted.
     *
     * @return Whether the pack is hosted.
     */
    public fun isHosted(): Boolean {
        return this.future.isDone && !this.future.isCompletedExceptionally
    }

    /**
     * Gets the [HostedPack] if it has finished being hosted.
     *
     * @return The hosted pack, or `null` if it is not ready.
     */
    public fun getNow(): HostedPack? {
        if (!this.future.isCompletedExceptionally) {
            return this.future.getNow(null)
        }
        return null
    }

    /**
     * Suspends until the pack has been hosted.
     *
     * @return The hosted pack.
     */
    public suspend fun await(): HostedPack {
        return this.future.await()
    }

    /**
     * Blocks the current thread until the pack has been hosted.
     *
     * @return The hosted pack.
     */
    public fun join(): HostedPack {
        return this.future.join()
    }

    /**
     * Gets a [CompletableFuture] which completes when the pack has been hosted.
     *
     * @return A future for the hosted pack.
     */
    public fun asFuture(): CompletableFuture<HostedPack> {
        return this.future.copy()
    }

    internal fun complete(hosted: HostedPack) {
        this.future.complete(hosted)
    }

    internal fun completeExceptionally(throwable: Throwable) {
        this.future.completeExceptionally(throwable)
    }
}
