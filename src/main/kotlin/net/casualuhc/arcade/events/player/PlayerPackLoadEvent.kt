package net.casualuhc.arcade.events.player

import net.minecraft.server.level.ServerPlayer

data class PlayerPackLoadEvent(
    override val player: ServerPlayer
): PlayerEvent