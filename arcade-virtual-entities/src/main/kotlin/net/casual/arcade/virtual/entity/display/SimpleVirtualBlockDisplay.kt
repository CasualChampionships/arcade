/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.display

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.level.block.state.BlockState
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors.Display.Block as BlockDisplayDataAccessors

public open class SimpleVirtualBlockDisplay(
    attachment: VirtualEntityAttachment
): SimpleVirtualDisplay(EntityType.BLOCK_DISPLAY, attachment) {
    public fun setBlockState(state: BlockState) {
        this.data.modifyEntry(BlockDisplayDataAccessors.BLOCK_STATE) { state }
    }

    public fun setBlockStateFor(observer: ServerPlayer, state: BlockState) {
        this.data.set(observer.uuid, BlockDisplayDataAccessors.BLOCK_STATE, state)
    }

    public fun setBlockStateToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, BlockDisplayDataAccessors.BLOCK_STATE)
    }

    public fun modifyBlockState(modifier: (BlockState) -> BlockState) {
        this.data.modifyEntry(BlockDisplayDataAccessors.BLOCK_STATE, false, modifier)
    }
}