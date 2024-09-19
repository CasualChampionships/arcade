package net.casual.arcade.events.player

import net.casual.arcade.events.core.CancellableEvent
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

public data class PlayerAttackEvent(
    override val player: ServerPlayer,
    val target: Entity,
    val damage: Float
): CancellableEvent.Typed<Float>(), PlayerEvent