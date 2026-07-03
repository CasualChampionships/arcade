/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.optional

import java.util.Optional

public fun <R: Any> (() -> R?).optional(): () -> Optional<R> {
    return { Optional.ofNullable(this.invoke()) }
}

public fun <A, R: Any> ((A) -> R?).optional(): (A) -> Optional<R> {
    return { a -> Optional.ofNullable(this.invoke(a)) }
}