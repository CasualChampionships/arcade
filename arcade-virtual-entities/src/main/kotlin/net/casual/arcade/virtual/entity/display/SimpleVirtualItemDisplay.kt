/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.virtual.entity.display

import net.casual.arcade.virtual.entity.attachment.VirtualEntityAttachment
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack
import net.casual.arcade.virtual.entity.utils.EntityDataAccessors.Display.Item as ItemDisplayDataAccessors

public open class SimpleVirtualItemDisplay(
    attachment: VirtualEntityAttachment
): SimpleVirtualDisplay(EntityType.ITEM_DISPLAY, attachment) {
    public fun setItemStack(stack: ItemStack) {
        this.data.modifyEntry(ItemDisplayDataAccessors.ITEM_STACK) { stack }
    }

    public fun setItemStackFor(observer: ServerPlayer, stack: ItemStack) {
        this.data.set(observer.uuid, ItemDisplayDataAccessors.ITEM_STACK, stack)
    }

    public fun setItemStackToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, ItemDisplayDataAccessors.ITEM_STACK)
    }

    public fun modifyItemStack(modifier: (ItemStack) -> ItemStack) {
        this.data.modifyEntry(ItemDisplayDataAccessors.ITEM_STACK, false, modifier)
    }

    public fun setItemDisplayContext(context: ItemDisplayContext) {
        this.data.modifyEntry(ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT) { context.id }
    }

    public fun setItemDisplayContextFor(observer: ServerPlayer, context: ItemDisplayContext) {
        this.data.set(observer.uuid, ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT, context.id)
    }

    public fun setItemDisplayContextToBaseFor(observer: ServerPlayer) {
        this.data.setToBase(observer.uuid, ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT)
    }

    public fun modifyItemDisplayContext(modifier: (ItemDisplayContext) -> ItemDisplayContext) {
        this.data.modifyEntry(ItemDisplayDataAccessors.ITEM_DISPLAY_CONTEXT, false) { current ->
            modifier.invoke(ItemDisplayContext.BY_ID.apply(current.toInt())).id
        }
    }
}