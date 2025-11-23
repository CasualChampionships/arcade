/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.entity.Entity
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.BlockHitResult

public sealed interface SlotInteractAction {
    public data object Swing: SlotInteractAction
    public data class AttackEntity(val target: Entity): SlotInteractAction
    public data class AttackBlock(val result: BlockPos, val direction: Direction): SlotInteractAction
    public data object Use: SlotInteractAction
    public data class UseOnEntity(val target: Entity): SlotInteractAction
    public data class UseOnBlock(val result: BlockHitResult): SlotInteractAction
    public data class PickBlock(val pos: BlockPos, val state: BlockState): SlotInteractAction
    public data class PickEntity(val entity: Entity): SlotInteractAction
    public data class Drop(val all: Boolean): SlotInteractAction
    public data object SwapOffhand: SlotInteractAction
}