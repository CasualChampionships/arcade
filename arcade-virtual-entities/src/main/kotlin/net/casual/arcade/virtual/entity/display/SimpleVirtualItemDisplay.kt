/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.display

import net.casual.arcade.observer.tracker.ObserverTracker
import net.casual.arcade.observer.tracker.SimpleObserverTracker
import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors.Display.Item as ItemDisplayDataAccessors

public open class SimpleVirtualItemDisplay(
    attachment: VirtualEntityAttachment,
    observers: ObserverTracker = SimpleObserverTracker()
): SimpleVirtualDisplay(EntityTypes.ITEM_DISPLAY, attachment, observers) {
    public fun setItemStack(stack: ItemStack) {
        this.setDataEntry(ItemDisplayDataAccessors.ITEM_STACK, stack)
    }

    public fun setItemStackFor(observer: ServerPlayer, stack: ItemStack) {
        this.setDataEntryFor(observer, ItemDisplayDataAccessors.ITEM_STACK, stack)
    }

    public fun setItemStackToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, ItemDisplayDataAccessors.ITEM_STACK)
    }

    public fun modifyItemStack(modifier: (ItemStack) -> ItemStack) {
        this.modifyDataEntry(ItemDisplayDataAccessors.ITEM_STACK, modifier)
    }

    public fun setItemDisplayContext(context: ItemDisplayContext) {
        this.setDataEntry(ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT, context.id)
    }

    public fun setItemDisplayContextFor(observer: ServerPlayer, context: ItemDisplayContext) {
        this.setDataEntryFor(observer, ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT, context.id)
    }

    public fun setItemDisplayContextToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT)
    }

    public fun modifyItemDisplayContext(modifier: (ItemDisplayContext) -> ItemDisplayContext) {
        this.modifyDataEntry(ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT) { current ->
            modifier.invoke(ItemDisplayContext.BY_ID.apply(current.toInt())).id
        }
    }
}