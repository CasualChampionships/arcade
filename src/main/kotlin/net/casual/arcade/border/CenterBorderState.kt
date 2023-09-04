package net.casual.arcade.border

import net.minecraft.world.level.border.BorderStatus
import org.joml.Vector2d

interface CenterBorderState {



    fun getCenter(): Vector2d

    fun update(): CenterBorderState
}