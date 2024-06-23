package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

/**
 * Supports PRE, POST.
 */
public data class PlayerDamageEvent(
    override val player: ServerPlayer,
    val amount: Float,
    val source: DamageSource
): CancellableEvent.Typed<Float>(), PlayerEvent