/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.molang.exception

public class MolangCompilationException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    public val source: String,
    message: String,
    cause: Throwable? = null
): RuntimeException("$message '${source}'", cause)