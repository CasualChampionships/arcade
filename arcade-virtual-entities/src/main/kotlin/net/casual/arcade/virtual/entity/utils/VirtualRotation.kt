package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.utils.MathUtils.minus
import net.casual.arcade.utils.MathUtils.plus
import net.casual.arcade.virtual.entity.VirtualEntity
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3

public sealed interface VirtualRotation {
    public fun get(origin: Vec2): Vec2

    public operator fun plus(offset: Vec2): VirtualRotation

    public operator fun minus(offset: Vec2): VirtualRotation

    public data class Absolute(val rotation: Vec2): VirtualRotation {
        override fun get(origin: Vec2): Vec2 {
            return this.rotation
        }

        override fun plus(offset: Vec2): VirtualRotation {
            return Absolute(this.rotation + offset)
        }

        override fun minus(offset: Vec2): VirtualRotation {
            return Absolute(this.rotation - offset)
        }
    }

    public data class Relative(val offset: Vec2): VirtualRotation {
        override fun get(origin: Vec2): Vec2 {
            return origin + this.offset
        }

        override fun plus(offset: Vec2): VirtualRotation {
            return Relative(this.offset + offset)
        }

        override fun minus(offset: Vec2): VirtualRotation {
            return Relative(this.offset - offset)
        }
    }

    public companion object {
        public val DEFAULT: VirtualRotation = Relative(Vec2.ZERO)
    }
}