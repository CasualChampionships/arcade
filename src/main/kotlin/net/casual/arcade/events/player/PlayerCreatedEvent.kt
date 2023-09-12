package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer

public data class PlayerCreatedEvent(
    override val player: ServerPlayer
): PlayerEvent