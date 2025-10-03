/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core

import net.minecraft.world.item.ItemStack

public interface GuiElement {
    public fun display(): ItemStack

    public fun click(action: SlotClickAction)
}