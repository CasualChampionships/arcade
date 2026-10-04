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

    public fun id(): String {
        return this.name.lowercase()
    }
}