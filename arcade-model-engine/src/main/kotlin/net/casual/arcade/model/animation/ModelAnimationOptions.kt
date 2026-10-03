/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation

public data class ModelAnimationOptions(
    val speed: Float = 1.0F,
    val weight: Float = 1.0F,
    val priority: Int = 0,
    val fadeIn: Int = 0,
    val fadeOut: Int = 0,
    val loop: AnimationLoop? = null,
    val override: Boolean? = null,
) {
    public companion object {
        public val DEFAULT: ModelAnimationOptions = ModelAnimationOptions()
    }
}