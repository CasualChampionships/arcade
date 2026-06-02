/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu.container

import net.minecraft.world.Container
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.ItemStack

internal object GuiContainer: Container {
    override fun getContainerSize(): Int {
        return 0
    }

    override fun isEmpty(): Boolean {
        return true
    }

    override fun getItem(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeItem(slot: Int, count: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removeItemNoUpdate(slot: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun setItem(slot: Int, itemStack: ItemStack) {

    }

    override fun setChanged() {

    }

    override fun stillValid(player: Player): Boolean {
        return false
    }

    override fun clearContent() {

    }
}