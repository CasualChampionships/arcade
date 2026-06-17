/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core.container

import it.unimi.dsi.fastutil.ints.IntArraySet
import net.casual.arcade.guis.menu.container.BindableContainerGuiMenu
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.guis.utils.SlotClickHandler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.Container
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.Slot

public open class BindableContainerGui(
    player: ServerPlayer,
    type: ContainerType,
    overrideInventory: Boolean
): ContainerGui(player, type, overrideInventory) {
    protected val bound: Array<Slot?> = arrayOfNulls(this.slots)

    private val dirtySlots = IntArraySet()

    public var areBoundSlotsInteractable: Boolean = true
    public var areInventorySlotsInteractable: Boolean = true
    public var isDroppingAllowed: Boolean = true

    public fun setSlot(slot: Int, container: Container, index: Int) {
        this.setSlot(slot, Slot(container, index, 0, 0))
    }

    public open fun setSlot(slot: Int, bound: Slot) {
        this.clearSlot(slot)
        this.bound[slot] = bound
    }

    override fun setSlot(slot: Int, item: GuiItem, handler: SlotClickHandler?) {
        super.setSlot(slot, item, handler)
        this.bound[slot] = null
        this.dirtySlots.add(slot)
    }

    override fun setSlotItem(slot: Int, item: GuiItem) {
        super.setSlotItem(slot, item)
        this.bound[slot] = null

        this.dirtySlots.add(slot)
    }

    override fun clearSlot(slot: Int) {
        super.clearSlot(slot)
        this.bound[slot] = null
        this.dirtySlots.add(slot)
    }

    override fun shouldIgnoreClick(slot: Int, action: SlotClickAction): Boolean {
        if (action.isDrop) {
            return this.isDroppingAllowed
        }
        if (this.bound.getOrNull(slot) != null) {
            return this.areBoundSlotsInteractable
        }
        if (!this.isInventoryOverridden() && (slot - this.slots) in 0..<Inventory.INVENTORY_SIZE) {
            return this.areInventorySlotsInteractable
        }
        return false
    }

    override fun createMenuProvider(): MenuProvider {
        return BindableContainerGuiMenu.Provider(this)
    }

    internal fun getBoundSlot(slot: Int): Slot? {
        return this.bound[slot]
    }

    internal fun checkDirtySlots(): IntArray {
        if (this.dirtySlots.isNotEmpty()) {
            val copy = this.dirtySlots.toIntArray()
            this.dirtySlots.clear()
            return copy
        }
        return intArrayOf()
    }
}