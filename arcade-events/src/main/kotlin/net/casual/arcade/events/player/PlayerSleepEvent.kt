package net.casual.arcade.events.player

import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer

public data class PlayerSleepEvent(
    override val player: ServerPlayer,
    val bedPosition: BlockPos
): PlayerEvent