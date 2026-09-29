/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.definition

import net.casual.arcade.model.animation.AnimationLoop
import net.casual.arcade.model.animation.timeline.BoneTimeline
import java.util.UUID

public class ModelAnimation private constructor(
    public val name: String,
    public val length: Float,
    public val loop: AnimationLoop,
    public val override: Boolean,
    public val startDelay: Float,
    public val loopDelay: Float,
    public val timelines: Map<UUID, BoneTimeline>
) {
    public companion object {
        public fun create(
            name: String,
            length: Float,
            loop: AnimationLoop,
            override: Boolean,
            startDelay: Float,
            loopDelay: Float,
            timelines: Map<UUID, BoneTimeline>
        ): ModelAnimation {
            return ModelAnimation(name, length, loop, override, startDelay, loopDelay, timelines.toMap())
        }
    }
}