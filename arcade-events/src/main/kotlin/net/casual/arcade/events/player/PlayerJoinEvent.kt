package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public data class PlayerJoinEvent(
    override val player: ServerPlayer
): CancellableEvent.Typed<Component>(), PlayerEvent