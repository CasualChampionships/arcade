/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.nametags.virtual

import net.casual.arcade.utils.ResourceUtils
import net.minecraft.world.entity.ai.attributes.AttributeInstance
import net.minecraft.world.entity.ai.attributes.AttributeModifier
import net.minecraft.world.entity.ai.attributes.Attributes

public class NametagHeight private constructor(
    public val height: Double
) {
    internal val attribute by lazy { createAttribute(this.height) }

    public companion object {
        private const val ARMOR_STAND_HEIGHT = 1.975
        private val SCALE_ID = ResourceUtils.arcade("nametag")

        public val DEFAULT: NametagHeight = of(0.275)
        public val INITIAL: NametagHeight = of(0.45)

        public fun of(height: Double): NametagHeight {
            require(height in (0.0625 * ARMOR_STAND_HEIGHT)..(16.0 * ARMOR_STAND_HEIGHT))
            return NametagHeight(height)
        }

        private fun createAttribute(height: Double): AttributeInstance? {
            val scale = height / ARMOR_STAND_HEIGHT
            if (scale == 1.0) {
                return null
            }
            val attribute = AttributeInstance(Attributes.SCALE) { }
            val modifier = AttributeModifier(SCALE_ID, scale - 1.0, AttributeModifier.Operation.ADD_MULTIPLIED_BASE)
            attribute.addPermanentModifier(modifier)
            return attribute
        }
    }
}