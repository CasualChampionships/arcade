/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core

import net.casual.arcade.guis.core.display.GuiItem
import net.casual.arcade.guis.menu.ContainerGuiMenu
import net.casual.arcade.guis.utils.ContainerType
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.guis.utils.SlotClickHandler
import net.casual.arcade.guis.utils.ensureMatchingPlayer
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

public open class ContainerGui(
    override val player: ServerPlayer,
    public val type: ContainerType,
    private val overrideInventory: Boolean,
): Gui {
    private val slots: Int = this.getContainerSize() + if (this.overrideInventory) Inventory.INVENTORY_SIZE else 0

    protected val items: Array<GuiItem?> = arrayOfNulls(this.slots)
    protected val handlers: Array<SlotClickHandler?> = arrayOfNulls(this.slots)

    private var title: Component = CommonComponents.EMPTY
    private var dirty: Boolean = false

    private var parent: Gui? = null

    public var canSpectatorsClick: Boolean = true

    public fun setSlot(slot: Int, item: GuiItem, handler: SlotClickHandler? = null) {
        this.checkSlotInBounds(slot)

        this.items[slot] = item
        this.handlers[slot] = handler
    }

    public fun setSlot(slot: Int, item: ItemStack, handler: SlotClickHandler? = null) {
        this.setSlot(slot, GuiItem(item), handler)
    }

    public fun setSlotItem(slot: Int, item: GuiItem) {
        this.checkSlotInBounds(slot)

        this.items[slot] = item
    }

    public fun setSlotItem(slot: Int, item: ItemStack) {
        this.setSlotItem(slot, GuiItem(item))
    }

    public fun clearSlot(slot: Int) {
        this.checkSlotInBounds(slot)

        this.items[slot] = null
        this.handlers[slot] = null
    }

    public open fun click(slot: Int, action: SlotClickAction) {
        this.handlers.getOrNull(slot)?.invoke(action)
    }

    public fun getSlotItem(slot: Int): GuiItem {
        return this.items.getOrNull(slot) ?: GuiItem.EMPTY
    }

    override fun tick() {
        for (item in this.items) {
            item?.tick()
        }
    }

    override fun createMenuProvider(): MenuProvider {
        return ContainerGuiMenu.Provider(this)
    }

    override fun getMenuType(): MenuType<*> {
        return this.type.menu
    }

    override fun setParent(parent: Gui?) {
        this.ensureMatchingPlayer(parent)
        this.parent = parent
    }

    override fun getParent(): Gui? {
        return this.parent
    }

    public fun isInventoryOverridden(): Boolean {
        return this.overrideInventory
    }

    public fun getSlotCount(): Int {
        return this.slots
    }

    public fun getContainerSize(): Int {
        return this.type.slots
    }

    public fun getTitle(): Component {
        return this.title
    }

    public fun setTitle(title: Component) {
        this.title = title
        this.markDirty()
    }

    public fun markDirty() {
        if (this.isOpen()) {
            this.dirty = true
        }
    }

    internal fun checkDirty(): Boolean {
        if (this.dirty) {
            this.dirty = false
            return true
        }
        return false
    }

    protected fun checkSlotInBounds(slot: Int) {
        require(slot in 0..<this.slots) { "Slot $slot is out of bounds for size ${this.slots}!" }
    }
}