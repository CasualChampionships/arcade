/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.display

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.casual.arcade.virtual.entity.observer.tracker.ObserverTracker
import net.casual.arcade.virtual.entity.observer.tracker.SimpleObserverTracker
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.level.block.state.BlockState
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors.Display.Block as BlockDisplayDataAccessors

public open class SimpleVirtualBlockDisplay(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker = SimpleObserverTracker()
): SimpleVirtualDisplay(EntityTypes.BLOCK_DISPLAY, attachment, observers) {
    public fun setBlockState(state: BlockState) {
        this.setDataEntry(BlockDisplayDataAccessors.BLOCK_STATE, state)
    }

    public fun setBlockStateFor(observer: ServerPlayer, state: BlockState) {
        this.setDataEntryFor(observer, BlockDisplayDataAccessors.BLOCK_STATE, state)
    }

    public fun setBlockStateToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, BlockDisplayDataAccessors.BLOCK_STATE)
    }

    public fun modifyBlockState(modifier: (BlockState) -> BlockState) {
        this.modifyDataEntry(BlockDisplayDataAccessors.BLOCK_STATE, modifier)
    }
}