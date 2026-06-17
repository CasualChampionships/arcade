/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu.book

import net.casual.arcade.guis.core.book.BookGui
import net.casual.arcade.guis.menu.slot.GuiSlot
import net.minecraft.world.item.ItemStack

public class BookGuiSlot(gui: BookGui, slot: Int): GuiSlot<BookGui>(gui, slot) {
    override fun getItem(): ItemStack {
        return this.gui.book
    }
}