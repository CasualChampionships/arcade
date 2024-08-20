package net.casual.arcade.gui.screen

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

    public fun setParent(parent: GuiInterface?) {
        if (parent == null) {
            this.parent = null
        } else if (parent.player == this.player) {
            this.parent = parent
        }
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

    override fun onClose() {
        this.openParent()
    }
}