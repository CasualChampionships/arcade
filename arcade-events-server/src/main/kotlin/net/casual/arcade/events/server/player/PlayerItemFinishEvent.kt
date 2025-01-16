/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack

public data class PlayerItemFinishEvent(
    override val player: ServerPlayer,
    val stack: ItemStack
): CancellableEvent.Typed<ItemStack>(), PlayerEvent