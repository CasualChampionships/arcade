package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

data class PlayerJoinEvent(
    override val player: ServerPlayer
): CancellableEvent.Typed<Component>(), PlayerEvent