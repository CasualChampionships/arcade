/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.screen

import eu.pb4.sgui.api.gui.GuiInterface
import eu.pb4.sgui.api.gui.SimpleGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType

public open class SimpleNestedGui(
    type: MenuType<*>,
    player: ServerPlayer,
    manipulatePlayerSlots: Boolean
): SimpleGui(type, player, manipulatePlayerSlots) {
    public var parent: GuiInterface? = null
        private set

    public fun setParent(parent: GuiInterface?): SimpleNestedGui {
        if (parent == null) {
            this.parent = null
        } else if (parent.player == this.player) {
            this.parent = parent
        }
        return this
    }

    public fun openParent() {
        this.parent?.open()
    }

    public fun openParentOrClose() {
        val parent = this.parent
        if (parent != null) {
            parent.open()
        } else {
            this.close()
        }
    }
}