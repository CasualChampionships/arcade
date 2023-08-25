package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer

data class PlayerLeaveEvent(
    override val player: ServerPlayer
): PlayerEvent