/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.format.blockbench.serializer

import net.casual.arcade.model.animation.timeline.KeyframeInterpolation
import net.casual.arcade.model.animation.timeline.KeyframeInterpolation.Bezier
import net.casual.arcade.model.animation.timeline.KeyframeInterpolation.CatmullRom
import net.casual.arcade.model.animation.timeline.KeyframeInterpolation.Linear
import net.casual.arcade.model.animation.timeline.KeyframeInterpolation.Step

internal object KeyframeInterpolationSerializer: InlinedStringSerializer<KeyframeInterpolation>(KeyframeInterpolation::id, ::parse)

private fun parse(id: String): KeyframeInterpolation {
    return when (id) {
        "linear" -> Linear
        "step" -> Step
        "catmullrom", "smooth" -> CatmullRom
        "bezier" -> Bezier
        else -> Linear
    }
}
