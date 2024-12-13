package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

/**
 * Supports PRE, POST.
 */
public data class PlayerDeathEvent(
    override val player: ServerPlayer,
    val source: DamageSource
): PlayerEvent