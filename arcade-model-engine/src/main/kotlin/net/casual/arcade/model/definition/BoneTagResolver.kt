/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.definition

public interface BoneTagResolver {
    public fun resolve(name: String, parentTags: Set<BoneTag>): Set<BoneTag>

    public object None: BoneTagResolver {
        override fun resolve(name: String, parentTags: Set<BoneTag>): Set<BoneTag> {
            return setOf()
        }
    }

    public object Default: BoneTagResolver {
        override fun resolve(name: String, parentTags: Set<BoneTag>): Set<BoneTag> {
            val tags = HashSet<BoneTag>(2)
            val lower = name.lowercase()
            when {
                lower.startsWith("head") -> tags.add(BoneTag.HEAD)
                lower.startsWith("hitbox") -> tags.add(BoneTag.HITBOX)
                lower.startsWith("seat") -> tags.add(BoneTag.SEAT)
            }
            if (BoneTag.HEAD in parentTags || BoneTag.HEAD_CHILD in parentTags) {
                tags.add(BoneTag.HEAD_CHILD)
            }
            return tags
        }
    }
}