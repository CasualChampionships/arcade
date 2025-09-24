/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

@OptIn(ExperimentalCoroutinesApi::class)
public fun <T: Any> Deferred<T>.getNow(default: T): T {
    return when {
        this.isCompleted -> this.getCompleted()
        else -> default
    }
}