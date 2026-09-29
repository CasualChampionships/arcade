/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.timeline

public enum class KeyframeInterpolation {
    Linear,
    Step,
    CatmullRom,
    Bezier;

    public companion object {
        public fun parseOrNull(name: String): KeyframeInterpolation? {
            return when (name) {
                "linear" -> Linear
                "step" -> Step
                "catmullrom", "smooth" -> CatmullRom
                "bezier" -> Bezier
                else -> null
            }
        }
    }
}