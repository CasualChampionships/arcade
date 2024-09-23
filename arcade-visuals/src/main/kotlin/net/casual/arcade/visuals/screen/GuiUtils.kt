package net.casual.arcade.visuals.screen

import eu.pb4.sgui.api.ClickType
import eu.pb4.sgui.api.gui.SlotGuiInterface
import net.casual.arcade.events.player.PlayerSlotClickEvent
import net.minecraft.world.item.ItemStack

public val PlayerSlotClickEvent.type: ClickType
    get() = ClickType.toClickType(this.action, this.button, this.index)

public fun SlotGuiInterface.setSlot(index: Int, stack: ItemStack, callback: () -> Unit) {
    this.setSlot(index, stack) { _, _, _, _ -> callback.invoke() }
}

public fun SlotGuiInterface.setSlot(index: Int, stack: ItemStack, callback: (ClickType) -> Unit) {
    this.setSlot(index, stack) { _, type, _, _ -> callback.invoke(type) }
}
