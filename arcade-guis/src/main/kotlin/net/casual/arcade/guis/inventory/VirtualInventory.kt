/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.inventory

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap
import net.casual.arcade.guis.utils.SlotClickAction
import net.casual.arcade.guis.utils.SlotClickHandler
import net.casual.arcade.guis.utils.SlotInteractAction
import net.casual.arcade.guis.utils.SlotInteractHandler
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.PlayerEquipment
import net.minecraft.world.item.ItemStack

public open class VirtualInventory(
    player: ServerPlayer
): CustomInventory(player, PlayerEquipment(player)) {
    private val clickHandlers = Int2ObjectOpenHashMap<SlotClickHandler>()
    private val interactHandlers = Int2ObjectOpenHashMap<SlotInteractHandler>()

    private val interactCounters = Object2IntOpenHashMap<SlotInteractAction>()

    private var defaultInteractHandler = SlotInteractHandler { true }
    private var interactionsPerTick = 1

    public fun setSlot(slot: Int, display: ItemStack, click: SlotClickHandler? = null, interact: SlotInteractHandler? = null) {
        this.checkSlotInBounds(slot)

        this.setItem(slot, display)
        if (click != null) {
            this.clickHandlers.put(slot, click)
        } else {
            this.clickHandlers.remove(slot)
        }
        if (interact != null) {
            require(isHotbarSlot(slot) || slot == SLOT_OFFHAND) { "Cannot set interaction handler for non-hotbar slot" }
            this.interactHandlers.put(slot, interact)
        } else {
            this.interactHandlers.remove(slot)
        }
    }

    public fun setSlotDisplay(slot: Int, display: ItemStack) {
        this.checkSlotInBounds(slot)

        this.setItem(slot, display)
    }

    public fun clearSlot(slot: Int) {
        this.checkSlotInBounds(slot)

        this.removeItemNoUpdate(slot)
        this.clickHandlers.remove(slot)
        this.interactHandlers.remove(slot)
    }

    public fun setDefaultInteractHandler(handler: SlotInteractHandler) {
        this.defaultInteractHandler = handler
    }

    public fun setInteractionsPerTick(interactions: Int) {
        this.interactionsPerTick = interactions
    }

    override fun menu(): CustomInventoryMenu {
        return VirtualInventoryMenu(this, this.player())
    }

    public open fun click(slot: Int, action: SlotClickAction) {
        this.clickHandlers.get(slot)?.invoke(action)
    }

    public open fun interact(slot: Int, action: SlotInteractAction): Boolean {
        if (this.interactCounters.addTo(action, 1) < this.interactionsPerTick) {
            val handler = this.interactHandlers.get(slot) ?: this.defaultInteractHandler
            return handler.invoke(action)
        }
        return true
    }

    override fun tick() {
        this.interactCounters.clear()
        super.tick()
    }

    final override fun getSlotWithRemainingSpace(stack: ItemStack): Int {
        return -1
    }

    final override fun add(slot: Int, stack: ItemStack): Boolean {
        return false
    }

    final override fun placeItemBackInInventory(stack: ItemStack, sendPacket: Boolean) {

    }

    override fun dropAll() {

    }

    protected fun checkSlotInBounds(slot: Int) {
        require(slot in 0..<this.containerSize) { "Slot $slot is out of bounds for size ${this.containerSize}!" }
    }
}