package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

data class PlayerGameModeChangeEvent(
    override val player: ServerPlayer,
    val previous: GameType,
    val current: GameType
): CancellableEvent.Default(), PlayerEvent