package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level

data class PlayerTNTPrimedEvent(
    val player: ServerPlayer,
    val level: Level,
    val pos: BlockPos
): CancellableEvent.Default()