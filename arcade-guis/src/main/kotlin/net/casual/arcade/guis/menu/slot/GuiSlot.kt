/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu.slot

import net.casual.arcade.guis.core.Gui
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack
import java.util.*

public abstract class GuiSlot<G: Gui>(
    protected val gui: G,
    slot: Int
): Slot(EmptyContainer, slot, 0, 0) {
    abstract override fun getItem(): ItemStack

    final override fun allowModification(player: Player): Boolean {
        return false
    }

    final override fun safeInsert(inputStack: ItemStack, inputAmount: Int): ItemStack {
        return inputStack
    }

    final override fun safeInsert(stack: ItemStack): ItemStack {
        return stack
    }

    final override fun tryRemove(amount: Int, maxAmount: Int, player: Player): Optional<ItemStack> {
        return Optional.empty()
    }

    final override fun mayPickup(player: Player): Boolean {
        return false
    }

    final override fun remove(amount: Int): ItemStack {
        return ItemStack.EMPTY
    }

    final override fun setChanged() {

    }

    final override fun setByPlayer(itemStack: ItemStack, previous: ItemStack) {

    }

    final override fun setByPlayer(itemStack: ItemStack) {

    }

    final override fun mayPlace(itemStack: ItemStack): Boolean {
        return false
    }
}