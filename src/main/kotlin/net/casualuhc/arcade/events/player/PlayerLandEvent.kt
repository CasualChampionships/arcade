package net.casualuhc.arcade.events.player

import net.casualuhc.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

data class PlayerLandEvent(
    val player: ServerPlayer,
    val damage: Int,
    val distance: Float,
    val multiplier: Float,
    val source: DamageSource
): CancellableEvent.Typed<Int>()