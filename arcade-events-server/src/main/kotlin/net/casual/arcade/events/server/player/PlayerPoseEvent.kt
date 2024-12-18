/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Pose

public data class PlayerPoseEvent(
    override val player: ServerPlayer,
    val previous: Pose,
    val updated: Pose
): PlayerEvent