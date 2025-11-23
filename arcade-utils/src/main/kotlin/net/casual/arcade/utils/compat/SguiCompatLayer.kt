/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.compat

import eu.pb4.sgui.api.GuiHelpers
import eu.pb4.sgui.api.SlotHolder
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.server.level.ServerPlayer

public object SguiCompatLayer {
    public val loaded: Boolean = FabricLoader.getInstance().isModLoaded("sgui")

    public fun isInGui(player: ServerPlayer): Boolean {
        if (!this.loaded) {
            return false
        }
        return GuiHelpers.getCurrentGui(player) != null
    }

    public fun isInGuiWithOverriddenInventory(player: ServerPlayer): Boolean {
        if (!this.loaded) {
            return false
        }
        val gui = GuiHelpers.getCurrentGui(player) as? SlotHolder ?: return false
        return gui.isIncludingPlayer
    }
}