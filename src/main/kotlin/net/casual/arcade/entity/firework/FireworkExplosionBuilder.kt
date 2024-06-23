package net.casual.arcade.entity.firework

import it.unimi.dsi.fastutil.ints.IntArrayList
import net.minecraft.world.item.component.FireworkExplosion
import net.minecraft.world.item.component.FireworkExplosion.Shape

public class FireworkExplosionBuilder {
    private val primaryColours = IntArrayList()
    private val fadeColours = IntArrayList()

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

    public fun addPrimaryColours(vararg colours: Int): FireworkExplosionBuilder {
        for (colour in colours) {
            this.primaryColours.add(colour)
        }
        return this
    }

    public fun addFadeColours(vararg colours: Int): FireworkExplosionBuilder {
        for (colour in colours) {
            this.fadeColours.add(colour)
        }
        return this
    }

    public fun build(): FireworkExplosion {
        return FireworkExplosion(this.shape, this.primaryColours, this.fadeColours, this.hasTrail, this.hasTwinkle)
    }
}