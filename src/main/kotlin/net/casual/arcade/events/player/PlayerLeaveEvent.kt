package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer

public data class PlayerLeaveEvent(
    override val player: ServerPlayer
): PlayerEvent