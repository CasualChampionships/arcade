package net.casual.arcade.events.player

import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

public data class PlayerDimensionChangeEvent(
    override val player: ServerPlayer,
    val destination: ServerLevel
): PlayerEvent