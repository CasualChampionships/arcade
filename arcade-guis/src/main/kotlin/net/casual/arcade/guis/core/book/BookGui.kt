/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core.book

import net.casual.arcade.guis.core.Gui
import net.casual.arcade.guis.menu.book.BookGuiMenu
import net.casual.arcade.guis.utils.BookClickAction
import net.casual.arcade.guis.utils.ensureMatchingPlayer
import net.minecraft.nbt.Tag
import net.minecraft.resources.Identifier
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.MenuProvider
import net.minecraft.world.inventory.MenuType
import net.minecraft.world.item.ItemStack

public open class BookGui(
    override val player: ServerPlayer,
    public var book: ItemStack = ItemStack.EMPTY
): Gui {
    private var page: Int = 0
    private var dirty: Boolean = false

    private var parent: Gui? = null

    public var canSpectatorsClick: Boolean = true

    public open fun click(action: BookClickAction) {
        when (action) {
            BookClickAction.TakeBook -> {}
            BookClickAction.NextPage -> this.setPage(this.page + 1)
            BookClickAction.PreviousPage -> this.setPage(this.page - 1)
            is BookClickAction.SetPage -> this.setPage(action.page)
        }
    }

    public open fun click(id: Identifier, payload: Tag?): Boolean {
        return false
    }

    public open fun setPage(page: Int) {
        this.page = page

        this.markDirty()
    }

    public fun getPage(): Int {
        return this.page
    }

    override fun createMenuProvider(): MenuProvider {
        return BookGuiMenu.Provider(this)
    }

    override fun getMenuType(): MenuType<*> {
        return MenuType.LECTERN
    }

    override fun setParent(parent: Gui?) {
        this.ensureMatchingPlayer(parent)
        this.parent = parent
    }

    override fun getParent(): Gui? {
        return this.parent
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
}