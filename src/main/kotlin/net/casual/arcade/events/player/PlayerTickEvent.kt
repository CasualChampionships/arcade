package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer

data class PlayerTickEvent(
    override val player: ServerPlayer
): PlayerEvent