/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.guis.hotbar

import net.minecraft.server.level.ServerPlayer

/**
 * How I want this to work:
 *   - Users can specify items in each hotbar slot - like existing sgui functionality
 *   - Users can specify passthrough slots, that is, slots the reference another slot
 *     items can be moved from these slots
 *   - When opening a container the hotbar gui should become 'dormant' and
 */
// What if instead of making a "Hotbar" gui, we just replace the entire player inventory.
// I think this is actually not a bad idea...
// We implement our own version of InventoryMenu that allows us to hook into events
// We need to be careful with how we implement this, since it's not virtual the items
// *do* exist on the server-side and could potentially be exploited.
public open class HotbarGui(
    public val player: ServerPlayer
) {

}