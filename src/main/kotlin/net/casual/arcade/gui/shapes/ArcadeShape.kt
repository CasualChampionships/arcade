package net.casual.arcade.gui.shapes

import net.casual.arcade.utils.ShapeUtils
import net.casual.arcade.utils.ShapeUtils.drawAsParticlesFor
import net.minecraft.world.phys.Vec3

/**
 * This interface represents a traversable shape which
 * can be iterated and then displayed with particles.
 * See [ShapeUtils.drawAsParticlesFor].
 */
public interface ArcadeShape: Iterable<Vec3> {
    /**
     * Returns an iterator with the default step of `10`.
     *
     * @return An [Iterator] with elements of [Vec3].
     */
    override fun iterator(): Iterator<Vec3> {
        return this.iterator(10)
    }

    /**
     * Returns an iterator with a specified number of steps
     * between the points.
     *
     * @param steps The number of steps to take.
     * @return An [Iterator] with elements of [Vec3].
     */
    public fun iterator(steps: Int): Iterator<Vec3>
}