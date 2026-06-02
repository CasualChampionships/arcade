/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu

import net.casual.arcade.guis.core.ContainerGui
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import java.util.*

public class GuiSlot(
    private val gui: ContainerGui,
    slot: Int
): Slot(GuiContainer, slot, 0, 0) {
    override fun getItem(): ItemStack {
        return this.gui.getSlotItem(this.containerSlot).display()
    }

    override fun allowModification(player: Player): Boolean {
        return false
    }

    override fun safeInsert(inputStack: ItemStack, inputAmount: Int): ItemStack {
        return inputStack
    }

    override fun safeInsert(stack: ItemStack): ItemStack {
        return stack
    }

    override fun tryRemove(amount: Int, maxAmount: Int, player: Player): Optional<ItemStack> {
        return Optional.empty()
    }

    override fun mayPickup(player: Player): Boolean {
        return false
    }

    override fun remove(amount: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun setChanged() {

    }

    override fun setByPlayer(itemStack: ItemStack, previous: ItemStack) {

    }

    override fun setByPlayer(itemStack: ItemStack) {

    }

    override fun mayPlace(itemStack: ItemStack): Boolean {
        return false
    }
}