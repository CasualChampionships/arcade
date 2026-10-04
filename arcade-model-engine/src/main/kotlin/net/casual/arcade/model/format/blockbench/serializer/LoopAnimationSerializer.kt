/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench.serializer

import net.casual.arcade.model.animation.AnimationLoop

internal object LoopAnimationSerializer: InlinedStringSerializer<AnimationLoop>(AnimationLoop::id, ::parse)

private fun parse(id: String): AnimationLoop {
    return AnimationLoop.entries.find { loop -> loop.id() == id } ?: AnimationLoop.Once
}
