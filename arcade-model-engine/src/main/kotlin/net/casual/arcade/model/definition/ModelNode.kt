/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.definition

import net.casual.arcade.model.geometry.BoneGeometry
import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.UUID

public sealed class ModelNode(
    public val name: String,
    public val uuid: UUID,
    public val pivot: Vector3fc,
    public val rotation: Vector3fc,
    private val tags: Set<BoneTag>,
    private val children: List<ModelNode>
) {
    // TODO:
    private var parent: ModelNode? = null

    public val isRoot: Boolean
        get() = this.parent == null

    public fun has(tag: BoneTag): Boolean {
        return this.tags.contains(tag)
    }

    public fun ancestors(): Sequence<ModelNode> {
        return generateSequence(this.parent, ModelNode::parent)
    }

    public fun descendents(): Sequence<ModelNode> {
        return this.children.asSequence().flatMap { child -> child.descendents() + child }
    }

    override fun toString(): String {
        return "${this::class.simpleName}(${this.name})"
    }

    public class Bone(
        name: String,
        uuid: UUID,
        pivot: Vector3fc,
        rotation: Vector3fc,
        tags: Set<BoneTag>,
        children: List<ModelNode>,
        public val geometry: BoneGeometry?
    ): ModelNode(name, uuid, Vector3f(pivot), Vector3f(rotation), tags.toSet(), children.toList())

    public class Locator(
        name: String,
        uuid: UUID,
        pivot: Vector3fc,
        rotation: Vector3fc,
        tags: Set<BoneTag>,
    ): ModelNode(name, uuid, Vector3f(pivot), Vector3f(rotation), tags.toSet(), listOf())
}