package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3

public data class PlayerMoveEvent(
    override val player: ServerPlayer,
    val position: Vec3
): PlayerEvent