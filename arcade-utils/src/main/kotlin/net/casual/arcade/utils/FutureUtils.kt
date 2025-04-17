/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future

public fun <T> Future<T>.asCompletableFuture(): CompletableFuture<T> {
    if (this is CompletableFuture) {
        return this
    }
    if (this.isDone) {
        val future = CompletableFuture<T>()
        try {
            future.complete(this.get())
        } catch (t: Throwable) {
            future.completeExceptionally(t)
        }
        return future
    }
    return CompletableFuture.supplyAsync(this::get)
}