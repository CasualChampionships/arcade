package net.casual.arcade.events.server.player

import net.casual.arcade.events.common.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.InteractionHand

public data class PlayerClientSwingHandEvent(
    override val player: ServerPlayer,
    val hand: InteractionHand
): CancellableEvent.Default(), PlayerEvent