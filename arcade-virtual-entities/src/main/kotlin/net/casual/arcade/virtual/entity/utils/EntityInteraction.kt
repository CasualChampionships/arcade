package net.casual.arcade.virtual.entity.utils

import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3

public sealed interface EntityInteraction {
    public data object Attack: EntityInteraction
    public data class Use(val hand: InteractionHand): EntityInteraction
    public data class UseAt(val hand: InteractionHand, val pos: Vec3): EntityInteraction
    public data class Pick(val includeData: Boolean): EntityInteraction
}