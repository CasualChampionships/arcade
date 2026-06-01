/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu

import net.casual.arcade.guis.core.Gui
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.Slot
import net.minecraft.world.item.ItemStack

public abstract class GuiMenu<G: Gui>(
    public val gui: G,
    containerId: Int
): AbstractContainerMenu(gui.getMenuType(), containerId) {
    public open fun tick() {
        this.gui.tick()
    }

    override fun stillValid(player: Player): Boolean {
        return this.gui.valid()
    }

    override fun canTakeItemForPickAll(carried: ItemStack, target: Slot): Boolean {
        return target !is GuiSlot && super.canTakeItemForPickAll(carried, target)
    }

    override fun removed(player: Player) {
        val reason = CLOSE_REASON.orElse(Gui.CloseReason.Unknown)
        this.gui.onClose(reason)
        super.removed(player)
    }

    public interface Provider<G: Gui>: MenuProvider {
        public val gui: G

        override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu
    }

    public companion object {
        @JvmField
        public val CLOSE_REASON: ScopedValue<Gui.CloseReason> = ScopedValue.newInstance()
    }
}