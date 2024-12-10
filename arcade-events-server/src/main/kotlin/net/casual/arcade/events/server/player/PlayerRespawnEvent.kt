package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer

public data class PlayerRespawnEvent(
    override val player: ServerPlayer
): PlayerEvent