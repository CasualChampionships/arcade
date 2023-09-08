package net.casual.arcade.gui.shapes

import net.minecraft.world.phys.Vec3

interface ArcadeShape: Iterable<Vec3> {
    override fun iterator(): Iterator<Vec3> {
        return this.iterator(10)
    }

    fun iterator(steps: Int): Iterator<Vec3>
}