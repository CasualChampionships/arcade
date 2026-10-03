/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.model.definition

@JvmInline
public value class BoneTag(public val name: String) {
    public companion object {
        public val HEAD: BoneTag = BoneTag("head")
        public val HEAD_CHILD: BoneTag = BoneTag("head_child")
        public val HITBOX: BoneTag = BoneTag("hitbox")
        public val SEAT: BoneTag = BoneTag("seat")
    }
}
