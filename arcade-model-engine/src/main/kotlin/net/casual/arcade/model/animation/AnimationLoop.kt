/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation

public enum class AnimationLoop {
    Once,
    Hold,
    Loop;

    public fun id(): String {
        return this.name.lowercase()
    }
}