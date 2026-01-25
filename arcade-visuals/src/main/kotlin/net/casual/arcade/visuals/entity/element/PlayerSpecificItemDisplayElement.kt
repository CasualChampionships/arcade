/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.entity.element

import eu.pb4.polymer.virtualentity.api.tracker.DisplayTrackedData
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

@Deprecated("Use arcade's virtual entity implementation instead")
public open class PlayerSpecificItemDisplayElement(): PlayerSpecificDisplayElement() {
    public constructor(stack: ItemStack): this() {
        this.setItemStack(stack)
    }

    public fun setItemStack(stack: ItemStack) {
        this.setDataEntry(DisplayTrackedData.Item.ITEM, stack)
    }

    public fun setItemStackFor(observer: ServerPlayer, stack: ItemStack) {
        this.setDataEntryFor(observer, DisplayTrackedData.Item.ITEM, stack)
    }

    public fun setItemStackToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.Item.ITEM)
    }

    public fun modifyItemStack(modifier: (ItemStack) -> ItemStack) {
        this.data.modifyEntry(DisplayTrackedData.Item.ITEM, false, modifier)
    }

    public fun setItemDisplayContext(context: ItemDisplayContext) {
        this.data.modifyEntry(DisplayTrackedData.Item.ITEM_DISPLAY) { context.id }
    }

    public fun setItemDisplayContextFor(observer: ServerPlayer, context: ItemDisplayContext) {
        this.data.set(observer.uuid, DisplayTrackedData.Item.ITEM_DISPLAY, context.id)
    }

    public fun setItemDisplayContextToBaseFor(observer: ServerPlayer) {
        this.setBaseDataEntryFor(observer, DisplayTrackedData.Item.ITEM_DISPLAY)
    }

    public fun modifyItemDisplayContext(modifier: (ItemDisplayContext) -> ItemDisplayContext) {
        this.modifyDataEntry(DisplayTrackedData.Item.ITEM_DISPLAY) { current ->
            modifier.invoke(ItemDisplayContext.BY_ID.apply(current.toInt())).id
        }
    }

    override fun getEntityType(): EntityType<*> {
        return EntityType.ITEM_DISPLAY
    }
}