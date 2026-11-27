/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.location

public sealed interface VirtualHeadRotation {
    public fun get(origin: Float): Float

    public operator fun plus(offset: Float): VirtualHeadRotation

    public operator fun minus(offset: Float): VirtualHeadRotation

    public class Absolute(public val rotation: Float): VirtualHeadRotation {
        override fun get(origin: Float): Float {
            return this.rotation
        }

        override fun plus(offset: Float): VirtualHeadRotation {
            return Absolute(this.rotation + offset)
        }

        override fun minus(offset: Float): VirtualHeadRotation {
            return Absolute(this.rotation - offset)
        }
    }

    public class Relative(public val offset: Float): VirtualHeadRotation {
        override fun get(origin: Float): Float {
            return origin + this.offset
        }

        override fun plus(offset: Float): VirtualHeadRotation {
            return Relative(this.offset + offset)
        }

        override fun minus(offset: Float): VirtualHeadRotation {
            return Relative(this.offset - offset)
        }
    }

    public companion object {
        public val DEFAULT: Absolute = Absolute(0.0F)
    }
}