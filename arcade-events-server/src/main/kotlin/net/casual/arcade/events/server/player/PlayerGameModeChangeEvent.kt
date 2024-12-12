package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

public data class PlayerGameModeChangeEvent(
    override val player: ServerPlayer,
    val previous: GameType,
    val current: GameType
): CancellableEvent.Default(), PlayerEvent