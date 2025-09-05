/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.firework

import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.FireworkExplosion.Shape

public class FireworkExplosionBuilder {
    private val primaryColors = IntArrayList()
    private val fadeColors = IntArrayList()

    public var shape: Shape = Shape.SMALL_BALL
    public var hasTrail: Boolean = false
    public var hasTwinkle: Boolean = false

    public fun shape(shape: Shape): FireworkExplosionBuilder {
        this.shape = shape
        return this
    }

    public fun trail(): FireworkExplosionBuilder {
        this.hasTrail = true
        return this
    }

    public fun twinkle(): FireworkExplosionBuilder {
        this.hasTwinkle = true
        return this
    }

    public fun addPrimaryColors(vararg colors: Int): FireworkExplosionBuilder {
        for (color in colors) {
            this.primaryColors.add(color)
        }
        return this
    }

    public fun addFadeColors(vararg colors: Int): FireworkExplosionBuilder {
        for (color in colors) {
            this.fadeColors.add(color)
        }
        return this
    }

    public fun build(): FireworkExplosion {
        return FireworkExplosion(this.shape, this.primaryColors, this.fadeColors, this.hasTrail, this.hasTwinkle)
    }
}