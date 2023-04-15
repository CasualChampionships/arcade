package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

data class PlayerAttackEvent(
    val player: ServerPlayer,
    val target: Entity,
    val damage: Float
): CancellableEvent.Typed<Float>()