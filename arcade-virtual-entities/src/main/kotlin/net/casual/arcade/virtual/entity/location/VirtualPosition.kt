/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.location

import net.casual.arcade.utils.MathUtils.minus
import net.casual.arcade.utils.MathUtils.plus
import net.minecraft.world.phys.Vec3

public sealed interface VirtualPosition {
    public fun get(origin: Vec3): Vec3

    public operator fun plus(offset: Vec3): VirtualPosition

    public operator fun minus(offset: Vec3): VirtualPosition

    public data class Absolute(val position: Vec3): VirtualPosition {
        override fun get(origin: Vec3): Vec3 {
            return this.position
        }

        override fun plus(offset: Vec3): VirtualPosition {
            return Absolute(this.position + offset)
        }

        override fun minus(offset: Vec3): VirtualPosition {
            return Absolute(this.position - offset)
        }
    }

    public data class Relative(val offset: Vec3): VirtualPosition {
        override fun get(origin: Vec3): Vec3 {
            return origin + this.offset
        }

        override fun plus(offset: Vec3): VirtualPosition {
            return Relative(this.offset + offset)
        }

        override fun minus(offset: Vec3): VirtualPosition {
            return Relative(this.offset - offset)
        }
    }

    public companion object {
        public val DEFAULT: VirtualPosition = Relative(Vec3.ZERO)
    }
}