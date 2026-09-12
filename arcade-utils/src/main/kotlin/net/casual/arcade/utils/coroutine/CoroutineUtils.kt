/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.coroutine

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration

public fun Job.joinBlocking() {
    val job = this
    runBlocking { job.join() }
}

public fun Job.joinBlocking(timeout: Duration) {
    val job = this
    runBlocking {
        withTimeout(timeout) { job.join() }
    }
}

public fun <T> Deferred<T>.awaitBlocking(): T {
    val deferred = this
    return runBlocking { deferred.await() }
}

public fun <T> Deferred<T>.awaitBlocking(timeout: Duration): T {
    val deferred = this
    return runBlocking {
        withTimeout(timeout) { deferred.await() }
    }
}

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