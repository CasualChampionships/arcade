package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer

public data class PlayerTryHarmEvent(
    override val player: ServerPlayer,
    val otherPlayer: ServerPlayer,
    var canHarmOtherBoolean: Boolean
): PlayerEvent