/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.menu.book

import net.casual.arcade.guis.core.book.BookGui
import net.casual.arcade.guis.menu.GuiMenu
import net.minecraft.network.chat.CommonComponents
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundContainerSetDataPacket
import net.minecraft.world.entity.player.Inventory
import net.minecraft.world.entity.player.Player
import net.minecraft.world.inventory.AbstractContainerMenu
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.block.entity.LecternBlockEntity

public open class BookGuiMenu(gui: BookGui, containerId: Int): GuiMenu<BookGui>(gui, containerId) {
    init {
        this.addSlot(BookGuiSlot(this.gui, 0))
    }

    override fun tick() {
        super.tick()

        if (this.gui.checkDirty()) {
            this.sendBookPage()
        }
    }

    override fun sendAllDataToRemote() {
        super.sendAllDataToRemote()

        this.sendBookPage()
    }

    override fun quickMoveStack(player: Player, slotIndex: Int): ItemStack {
        return ItemStack.EMPTY
    }

    private fun sendBookPage() {
        this.gui.player.connection.send(
            ClientboundContainerSetDataPacket(this.containerId, LecternBlockEntity.DATA_PAGE, this.gui.getPage())
        )
    }

    public class Provider(override val gui: BookGui): GuiMenu.Provider<BookGui> {
        override fun getDisplayName(): Component {
            return CommonComponents.EMPTY
        }

        override fun createMenu(containerId: Int, inventory: Inventory, player: Player): AbstractContainerMenu {
            return BookGuiMenu(this.gui, containerId)
        }
    }
}