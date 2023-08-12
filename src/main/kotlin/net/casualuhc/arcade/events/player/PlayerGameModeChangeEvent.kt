package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.GameType

class PlayerGameModeChangeEvent(
    val player: ServerPlayer,
    val previous: GameType,
    val current: GameType
): CancellableEvent.Default()