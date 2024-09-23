package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.damagesource.DamageSource

public data class PlayerTotemEvent(
    override val player: ServerPlayer,
    val source: DamageSource
): PlayerEvent