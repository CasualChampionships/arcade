package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

data class PlayerItemFinishEvent(
    override val player: ServerPlayer,
    val stack: ItemStack,
    val level: Level
): CancellableEvent.Typed<ItemStack>(), PlayerEvent