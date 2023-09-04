package net.casual.arcade.events.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Pose

data class PlayerPoseEvent(
    override val player: ServerPlayer,
    val previous: Pose,
    val updated: Pose
): PlayerEvent