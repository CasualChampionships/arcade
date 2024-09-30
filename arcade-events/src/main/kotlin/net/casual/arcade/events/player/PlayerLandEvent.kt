package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

public data class PlayerLandEvent(
    override val player: ServerPlayer,
    var damage: Int,
    val distance: Float,
    val multiplier: Float,
    val source: DamageSource
): CancellableEvent.Default(), PlayerEvent