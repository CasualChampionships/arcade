package net.casual.arcade.resources.event

import net.casual.arcade.events.player.PlayerEvent
import net.casual.arcade.resources.pack.PackState
import net.minecraft.server.level.ServerPlayer

public data class PlayerPackSuccessEvent(
    override val player: ServerPlayer,
    val packs: Collection<PackState>
): PlayerEvent