/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu

import net.casual.arcade.guis.core.ContainerGui
import net.casual.arcade.guis.mixins.core.AbstractContainerMenuAccessor
import net.casual.arcade.guis.utils.invalidateRemoteSlots
import net.casual.arcade.utils.player.updateOffhandSlot
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.network.protocol.game.ClientboundContainerSetContentPacket
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.inventory.InventoryMenu
import net.minecraft.world.item.ItemStack

public open class ContainerGuiMenu(
    gui: ContainerGui,
    containerId: Int,
    inventory: Inventory
): GuiMenu<ContainerGui>(gui, containerId) {
    init {
        this.initialize(inventory)
    }

    override fun tick() {
        super.tick()

        if (this.gui.checkDirty()) {
            this.resendGui()
        }
    }

    protected open fun initialize(inventory: Inventory) {
        val size = this.gui.getContainerSize()
        for (i in 0..<size) {
            this.addSlot(GuiSlot(this.gui, i))
        }

        if (this.gui.isInventoryOverridden()) {
            for (i in 0..<Inventory.INVENTORY_SIZE) {
                this.addSlot(GuiSlot(this.gui, size + i))
            }
        } else {
            this.addStandardInventorySlots(inventory, 0, 0)
        }
    }

    override fun quickMoveStack(player: Player, slotIndex: Int): ItemStack {
        return ItemStack.EMPTY
    }

    override fun removed(player: Player) {
        super.removed(player)
        if (this.gui.isInventoryOverridden()) {
            player.inventoryMenu.invalidateRemoteSlots()
            (player as ServerPlayer).updateOffhandSlot()
        }
    }

    override fun sendAllDataToRemote() {
        super.sendAllDataToRemote()
        if (this.gui.isInventoryOverridden()) {
            val player = this.gui.player
            val state = player.inventoryMenu.incrementStateId()
            player.connection.send(ClientboundContainerSetSlotPacket(0, state, InventoryMenu.SHIELD_SLOT, ItemStack.EMPTY))
        }
    }

    protected fun resendGui() {
        val packets = listOf(
            ClientboundOpenScreenPacket(this.containerId, this.type, this.gui.getTitle()),
            ClientboundContainerSetContentPacket(this.containerId, this.stateId, this.items, this.carried)
        )
        this.gui.player.connection.send(ClientboundBundlePacket(packets))

        for ((i, slot) in this.slots.withIndex()) {
            this.setRemoteSlot(i, slot.item.copy())
        }
        (this as AbstractContainerMenuAccessor).arcade_getRemoteCarried().force(this.carried.copy())
    }

    public class Provider(override val gui: ContainerGui): GuiMenu.Provider<ContainerGui> {
        override fun getDisplayName(): Component {
            return this.gui.getTitle()
        }

        override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
            return ContainerGuiMenu(this.gui, containerId, inventory)
        }
    }
}