/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation

import net.casual.arcade.model.animation.pose.BoneTransform
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelNode

public class ModelAnimator(
    private val definition: ModelDefinition
) {
    private val instances = ArrayList<ModelAnimationInstance>()

    public fun animate(node: ModelNode, dest: BoneTransform) {
        // TODO:
    }
}