package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer

public data class PlayerSetSneakingEvent(
    override val player: ServerPlayer,
    val sneaking: Boolean
): PlayerEvent