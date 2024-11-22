package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public data class PlayerTryAttackEvent(
    override val player: ServerPlayer,
    val target: Entity,
): CancellableEvent.Default(), PlayerEvent