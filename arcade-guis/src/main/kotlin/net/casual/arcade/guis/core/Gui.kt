/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.core

import net.casual.arcade.guis.utils.getOpenGui
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.inventory.MenuType
import org.jetbrains.annotations.ApiStatus.OverrideOnly

// What guis do we actually want to support?
//  - Chests
//  - Books
// We can figure the rest out later
public interface Gui {
    public val player: ServerPlayer

    @OverrideOnly
    public fun tick() {

    }

    @OverrideOnly
    public fun valid(): Boolean {
        return true
    }

    public fun open(): Boolean

    public fun isOpen(): Boolean {
        return this.player.getOpenGui() == this
    }

    public fun getMenuType(): MenuType<*>
}