/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.RandomizableContainer
import net.minecraft.world.item.ItemStack

public data class PlayerLootEvent(
    override val player: ServerPlayer,
    val items: List<ItemStack>,
    val container: RandomizableContainer
): PlayerEvent