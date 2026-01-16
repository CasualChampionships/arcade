/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.element

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.state.BlockState

public class PlayerSpecificBlockDisplayElement(): PlayerSpecificDisplayElement() {
    public constructor(state: BlockState): this() {
        this.setBlockState(state)
    }

    public fun setBlockState(state: BlockState) {
        this.data.modifyEntry(DisplayTrackedData.Block.BLOCK_STATE) { state }
    }

    public fun setBlockStateFor(observer: ServerPlayer, state: BlockState) {
        this.data.set(observer.uuid, DisplayTrackedData.Block.BLOCK_STATE, state)
    }

    public fun modifyBlockState(modifier: (BlockState) -> BlockState) {
        this.data.modifyEntry(DisplayTrackedData.Block.BLOCK_STATE, false, modifier)
    }

    override fun getEntityType(): EntityType<*> {
        return EntityType.BLOCK_DISPLAY
    }
}