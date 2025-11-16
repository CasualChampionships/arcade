/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.inventory

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import net.casual.arcade.guis.core.SlotClickAction
import net.casual.arcade.guis.core.SlotInteractAction
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.PlayerEquipment
import net.minecraft.world.item.ItemStack

public open class VirtualInventory(
    player: ServerPlayer
): CustomInventory(player, PlayerEquipment(player)) {
    private val clickHandlers = Int2ObjectOpenHashMap<ClickHandler>()
    private val interactHandlers = Int2ObjectOpenHashMap<InteractHandler>()

    private var defaultInteractHandler = InteractHandler { false }

    public fun setSlot(slot: Int, display: ItemStack, click: ClickHandler? = null, interact: InteractHandler? = null) {
        this.setItem(slot, display)
        if (click != null) {
            this.clickHandlers.put(slot, click)
        }
        if (interact != null) {
            require(isHotbarSlot(slot) || slot == SLOT_OFFHAND) { "Cannot set interaction handler for non-hotbar slot" }
            this.interactHandlers.put(slot, interact)
        }
    }

    public fun clearSlot(slot: Int) {
        this.removeItemNoUpdate(slot)
        this.clickHandlers.remove(slot)
        this.interactHandlers.remove(slot)
    }

    public fun setDefaultInteractHandler(handler: InteractHandler) {
        this.defaultInteractHandler = handler
    }

    override fun menu(): CustomInventoryMenu {
        return VirtualInventoryMenu(this, this.player())
    }

    public open fun click(slot: Int, action: SlotClickAction) {
        this.clickHandlers.get(slot)?.invoke(action)
    }

    public open fun interact(slot: Int, action: SlotInteractAction): Boolean {
        val handler = this.interactHandlers.get(slot) ?: this.defaultInteractHandler
        return handler.invoke(action)
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

    public fun interface ClickHandler {
        /**
         * Handles a slot click action.
         *
         * @param action The action that was performed.
         */
        public fun invoke(action: SlotClickAction)
    }

    public fun interface InteractHandler {
        /**
         * Handles some slot interaction with the world.
         *
         * @param action The action that was performed.
         * @return Whether the interaction was consumed.
         */
        public fun invoke(action: SlotInteractAction): Boolean
    }
}