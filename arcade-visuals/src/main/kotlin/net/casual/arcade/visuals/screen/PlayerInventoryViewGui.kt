/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.screen

import net.casual.arcade.utils.ItemUtils
import net.casual.arcade.utils.ItemUtils.named
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.inventory.Slot

public open class PlayerInventoryViewGui(
    protected val observee: ServerPlayer,
    observer: ServerPlayer
): SimpleNestedGui(MenuType.GENERIC_9x6, observer, false) {
    override fun onOpen() {
        super.onOpen()

        this.loadBackground()
        this.loadInventory()
    }

    override fun onTick() {
        if (this.observee.isRemoved) {
            this.close()
            return
        }
        this.updateMainhandSlot()
    }

    protected open fun updateMainhandSlot() {
        val inventory = this.observee.inventoryMenu
        val slot = inventory.getSlot(this.observee.inventory.selected + InventoryMenu.USE_ROW_SLOT_START)
        this.copySlotAndRedirect(this.mapMainhandSlot(), slot)
    }

    protected open fun loadInventory() {
        val inventory = this.observee.inventoryMenu
        val crafting = inventory.inputGridSlots
        this.copySlotAndRedirect(this.mapOffhandSlot(), inventory.getSlot(InventoryMenu.SHIELD_SLOT))
        for ((i, slot) in crafting.withIndex()) {
            this.copySlotAndRedirect(this.mapCraftingInputSlot(i), slot)
        }
        val armor = inventory.slots.subList(InventoryMenu.ARMOR_SLOT_START, InventoryMenu.ARMOR_SLOT_END)
        for ((i, slot) in armor.withIndex()) {
            this.copySlotAndRedirect(this.mapArmorSlot(i), slot)
        }
        val content = inventory.slots.subList(InventoryMenu.INV_SLOT_START, InventoryMenu.INV_SLOT_END)
        for ((i, slot) in content.withIndex()) {
            this.copySlotAndRedirect(this.mapInventorySlot(i), slot)
        }
        val hotbar = inventory.slots.subList(InventoryMenu.USE_ROW_SLOT_START, InventoryMenu.USE_ROW_SLOT_END)
        for ((i, slot) in hotbar.withIndex()) {
            this.copySlotAndRedirect(this.mapHotbarSlot(i), slot)
        }
    }

    protected open fun loadBackground() {
        this.title = this.observee.displayName

        this.setSlot(3, ItemUtils.createPlayerHead(this.observee).named(this.observee.displayName!!))
    }

    protected open fun mapOffhandSlot(): Int {
        return 1
    }

    protected open fun mapMainhandSlot(): Int {
        return 10
    }

    protected open fun mapCraftingInputSlot(index: Int): Int {
        // 0 -> 6, 1 -> 7, 2 -> 15, 3 -> 16
        return 6 + 7 * (index / 2) + index
    }

    protected open fun mapArmorSlot(index: Int): Int {
        // 0 -> 2, 1 -> 11, 2 -> 4, 3 -> 13
        return 9 * index - 16 * (index / 2) + 2
    }

    protected open fun mapInventorySlot(index: Int): Int {
        return index + 18
    }

    protected open fun mapHotbarSlot(index: Int): Int {
        return index + 45
    }

    private fun copySlotAndRedirect(index: Int, slot: Slot) {
        // We want to copy the slot to maintain the original slot's index
        val copy = Slot(slot.container, slot.containerSlot, slot.x, slot.y)
        this.setSlotRedirect(index, copy)
    }
}