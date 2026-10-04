/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.molang.exception

import net.casual.arcade.model.animation.molang.MolangExpression

public class MolangEvaluationException(
    @Suppress("CanBeParameter", "RedundantSuppression")
    public val expression: MolangExpression,
    message: String,
    cause: Throwable? = null
): RuntimeException("$message '${expression.source}'", cause)