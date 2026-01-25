/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.utils

import net.casual.arcade.virtual.entity.mixins.EntityAccessor
import net.casual.arcade.virtual.entity.mixins.LivingEntityAccessor
import net.minecraft.world.entity.Display.TextDisplay
import kotlin.experimental.and
import kotlin.experimental.or

public object EntityDataSharedFlags {
    public val ON_FIRE: Int = EntityAccessor.accessOnFireFlag()
    public val SHIFT_KEY_DOWN: Int = EntityAccessor.accessShiftKeyDownFlag()
    public val SPRINTING: Int = EntityAccessor.accessSprintingFlag()
    public val SWIMMING: Int = EntityAccessor.accessSwimmingFlag()
    public val INVISIBLE: Int = EntityAccessor.accessInvisibleFlag()
    public val GLOWING: Int = EntityAccessor.accessGlowingFlag()
    public val FALL_FLYING: Int = EntityAccessor.accessFallFlyingFlag()

    public fun updateFlag(flags: Byte, flag: Int, value: Boolean): Byte {
        return if (value) {
            flags or (1 shl flag).toByte()
        } else {
            flags and (1 shl flag).inv().toByte()
        }
    }

    public fun updateFlag(flags: Byte, flag: Byte, value: Boolean): Byte {
        return this.updateFlag(flags, flag.toInt(), value)
    }

    public object LivingEntity {
        public val IS_USING: Int = LivingEntityAccessor.accessIsUsingFlag()
        public val OFF_HAND: Int = LivingEntityAccessor.accessOffHandFlag()
        public val SPIN_ATTACK: Int = LivingEntityAccessor.accessSpinAttackFlag()
    }

    public object Display {
        public object Text {
            public const val SHADOW: Byte = TextDisplay.FLAG_SHADOW
            public const val SEE_THROUGH: Byte = TextDisplay.FLAG_SEE_THROUGH
            public const val USE_DEFAULT_BACKGROUND: Byte = TextDisplay.FLAG_USE_DEFAULT_BACKGROUND
            public const val ALIGN_LEFT: Byte = TextDisplay.FLAG_ALIGN_LEFT
            public const val ALIGN_RIGHT: Byte = TextDisplay.FLAG_ALIGN_RIGHT
        }
    }
}