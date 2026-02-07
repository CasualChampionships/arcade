/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils

public object JsonFormatUtils {
    private const val NUMBER = "-?\\d+(?:\\.\\d+)?(?:[eE][+-]?\\d+)?"

    private val COMPACT_ARRAY_2_3 = Regex(
        "\\[\\s*($NUMBER)\\s*,\\s*($NUMBER)\\s*(?:,\\s*($NUMBER)\\s*)?]"
    )

    public fun compactSmallNumericArrays(json: String): String {
        return COMPACT_ARRAY_2_3.replace(json) { match ->
            val a = match.groupValues[1]
            val b = match.groupValues[2]
            val c = match.groupValues.getOrNull(3)

            if (!c.isNullOrEmpty()) {
                "[$a, $b, $c]"
            } else {
                "[$a, $b]"
            }
        }
    }
}