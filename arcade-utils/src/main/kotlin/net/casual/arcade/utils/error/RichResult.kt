/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.error

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

@JvmInline
@Suppress("UNCHECKED_CAST")
public value class RichResult<out T> private constructor(
    @PublishedApi internal val value: Any?
) {
    public val isSuccess: Boolean
        get() = this.value !is Failure

    public val isFailure: Boolean
        get() = this.value is Failure

    public fun getOrNull(): T? {
        return when (this.value) {
            is Failure -> null
            else -> this.value as T
        }
    }

    public fun messageOrNull(): String? {
        return when (this.value) {
            is Failure -> this.value.message
            else -> null
        }
    }

    @OptIn(ExperimentalContracts::class)
    public inline fun dispatch(
        success: (T) -> Unit,
        failure: (String) -> Unit
    ) {
        contract {
            callsInPlace(success, InvocationKind.AT_MOST_ONCE)
            callsInPlace(failure, InvocationKind.AT_MOST_ONCE)
        }
        when (this.value) {
            is Failure -> failure.invoke(this.value.message)
            else -> success.invoke(this.value as T)
        }
    }

    @PublishedApi
    internal data class Failure(val message: String)

    public companion object {
        public fun <T> success(value: T): RichResult<T> {
            return RichResult(value)
        }

        public fun <T> failure(message: String): RichResult<T> {
            return RichResult(Failure(message))
        }
    }
}