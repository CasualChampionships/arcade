/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import net.casual.arcade.utils.coroutine.getNowOrNull

public fun <T> Deferred<T>.getNow(default: T & Any): T & Any {
    return this.getNowOrNull() ?: default
}

@OptIn(ExperimentalCoroutinesApi::class)
public fun <T> Deferred<T>.getNowOrNull(): T? {
    return when {
        this.isCompleted -> this.getCompleted()
        else -> null
    }
}