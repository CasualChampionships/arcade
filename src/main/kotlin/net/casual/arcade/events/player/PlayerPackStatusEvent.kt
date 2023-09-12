package net.casual.arcade.events.player

import net.minecraft.network.protocol.game.ServerboundResourcePackPacket.Action
import net.minecraft.server.level.ServerPlayer

public data class PlayerPackStatusEvent(
    override val player: ServerPlayer,
    val status: Action
): PlayerEvent