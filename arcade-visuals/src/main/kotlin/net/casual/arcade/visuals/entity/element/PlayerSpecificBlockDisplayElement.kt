/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.element

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.state.BlockState

@Deprecated("Use arcade's virtual entity implementation instead")
public class PlayerSpecificBlockDisplayElement(): PlayerSpecificDisplayElement() {
    public constructor(state: BlockState): this() {
        this.setBlockState(state)
    }

    public fun setBlockState(state: BlockState) {
        this.setDataEntry(DisplayTrackedData.Block.BLOCK_STATE, state)
    }

    public fun setBlockStateFor(observer: ServerPlayer, state: BlockState) {
        this.setDataEntryFor(observer, DisplayTrackedData.Block.BLOCK_STATE, state)
    }

    public fun setBlockStateToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.Block.BLOCK_STATE)
    }

    public fun modifyBlockState(modifier: (BlockState) -> BlockState) {
        this.data.modifyEntry(DisplayTrackedData.Block.BLOCK_STATE, false, modifier)
    }

    override fun getEntityType(): EntityType<*> {
        return EntityType.BLOCK_DISPLAY
    }
}