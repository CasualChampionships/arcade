package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.level.LevelEvent
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer

interface PlayerEvent: LevelEvent {
    val player: ServerPlayer

    override val level: ServerLevel
        get() = this.player.level() as ServerLevel
}