package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

data class PlayerTNTPrimedEvent(
    override val player: ServerPlayer,
    /**
     * The [ServerLevel] where the TNT is being primed.
     * This is not necessarily the same as [player]'s level.
     */
    override val level: ServerLevel,
    val pos: BlockPos
): CancellableEvent.Default(), PlayerEvent