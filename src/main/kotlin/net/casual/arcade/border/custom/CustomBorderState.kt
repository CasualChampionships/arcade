package net.casual.arcade.border.custom

import net.casual.arcade.border.state.CenterBorderState
import net.casual.arcade.border.state.CenterBorderStatus
import net.minecraft.world.level.border.BorderStatus
import net.minecraft.world.phys.Vec3

public interface CustomBorderState {




    public fun getCenter(): Vec3

    public fun getTargetCenter(): Vec3



    public fun getLerpRemainingTicks(): Long {
        TODO("Add this later!")
    }


    public fun update(): CustomBorderState

    public fun getStatus(): CenterBorderStatus

    public fun getType(): BorderStatus



}