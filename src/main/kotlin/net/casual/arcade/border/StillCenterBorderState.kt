package net.casual.arcade.border

import org.joml.Vector2d

class StillCenterBorderState(
    private var center: Vector2d,
): CenterBorderState {


    override fun getCenter(): Vector2d {
        return center
    }

    override fun update(): CenterBorderState {
        return this
    }
}