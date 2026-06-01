/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core.display

import net.minecraft.world.item.ItemStack

public interface GuiItem {
    public fun tick()

    public fun display(): ItemStack

    private class Simple(val display: ItemStack): GuiItem {
        override fun tick() {

        }

        override fun display(): ItemStack {
            return this.display
        }
    }

    public companion object {
        public val EMPTY: GuiItem = GuiItem(ItemStack.EMPTY)

        public operator fun invoke(item: ItemStack): GuiItem {
            return Simple(item)
        }
    }
}