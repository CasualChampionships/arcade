/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.interaction

import net.minecraft.world.InteractionHand
import net.minecraft.world.phys.Vec3

public sealed interface EntityInteraction {
    public data object Attack: EntityInteraction
    public data class Use(val hand: InteractionHand): EntityInteraction
    public data class UseAt(val hand: InteractionHand, val pos: Vec3): EntityInteraction
    public data class Pick(val includeData: Boolean): EntityInteraction
}