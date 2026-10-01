/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.timeline

import net.casual.arcade.model.animation.ModelAnimationInstance
import net.minecraft.resources.Identifier

public sealed interface EffectKeyframe {
    public val time: Float

    public data class Sound(override val time: Float, val sound: Identifier): EffectKeyframe

    public data class Command(override val time: Float, val value: String): EffectKeyframe

    public fun interface Handler {
        public fun handle(animation: ModelAnimationInstance, keyframe: EffectKeyframe)
    }
}