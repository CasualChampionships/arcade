/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.definition

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import net.minecraft.resources.Identifier

public class ModelDefinition private constructor(
    public val id: Identifier,
    public val roots: List<ModelNode>,
    public val textures: List<ModelTexture>,
    public val width: Float,
    public val height: Float,
    private val animations: Map<String, ModelAnimation>,
    private val nodesByName: Map<String, ModelNode>
) {
    public fun nodes(): Collection<ModelNode> {
        return this.nodesByName.values
    }

    public fun bones(): Sequence<ModelNode.Bone> {
        return this.nodes().asSequence().filterIsInstance<ModelNode.Bone>()
    }

    public fun locators(): Sequence<ModelNode.Locator> {
        return this.nodes().asSequence().filterIsInstance<ModelNode.Locator>()
    }

    public fun animations(): Collection<ModelAnimation> {
        return this.animations.values
    }

    public fun node(name: String): ModelNode? {
        return this.nodesByName[name]
    }

    public fun bone(name: String): ModelNode.Bone? {
        return this.nodesByName[name] as? ModelNode.Bone
    }

    public fun locator(name: String): ModelNode.Locator? {
        return this.nodesByName[name] as? ModelNode.Locator
    }

    public fun animation(name: String): ModelAnimation? {
        return this.animations[name]
    }

    public companion object {
        public fun create(
            id: Identifier,
            roots: List<ModelNode>,
            animations: Map<String, ModelAnimation>,
            textures: List<ModelTexture>,
            width: Float,
            height: Float
        ): ModelDefinition {
            val byName = Object2ObjectLinkedOpenHashMap<String, ModelNode>()
            // val byUUID = Object2ObjectLinkedOpenHashMap<UUID, ModelNode>()
            for (root in roots) {
                for (node in root.descendents() + root) {
                    require(byName.put(node.name, node) == null) { "Duplicated node name '${node.name}' for model $id" }
                    // require(byUUID.put(node.uuid, node) == null) { "Duplicated node uuid '${node.uuid}' for model $id" }
                }
            }

            return ModelDefinition(id, roots.toList(), textures.toList(), width, height, animations.toMap(), byName)
        }
    }
}