/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.animation.pose

import net.casual.arcade.model.animation.ModelAnimator
import net.casual.arcade.model.definition.ModelDefinition
import net.casual.arcade.model.definition.ModelNode
import net.minecraft.util.Mth
import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.*

internal class ModelPoser(
    private val definition: ModelDefinition
) {
    private val transform = BoneTransform()
    private val root = Matrix4f()
    private val rotation = Quaternionf()
    private val offset = Vector3f()

    fun pose(animator: ModelAnimator, scale: Float, poses: Map<UUID, BonePose>) {
        this.root.rotationY(Mth.PI).scale(scale)
        for (node in this.definition.roots) {
            this.pose(node, this.root, animator, poses)
        }
    }

    private fun pose(
        node: ModelNode,
        parent: Matrix4fc,
        animator: ModelAnimator,
        poses: Map<UUID, BonePose>
    ) {
        val transform = this.transform.identity()
        animator.animate(node, transform)

        val pose = poses.getValue(node.uuid).raw().set(parent)
        val rotation = transform.rotation
        this.offset.set(transform.position).div(16.0F).add(node.pivot)
        pose.translate(this.offset)
        this.rotation.rotationZYX(
            Mth.DEG_TO_RAD * (rotation.z + node.rotation.z()),
            Mth.DEG_TO_RAD * (rotation.y + node.rotation.y()),
            Mth.DEG_TO_RAD * (rotation.x + node.rotation.x())
        )
        pose.rotate(this.rotation)
        pose.scale(transform.scale)

        for (child in node.children()) {
            this.pose(child, pose, animator, poses)
        }
    }
}