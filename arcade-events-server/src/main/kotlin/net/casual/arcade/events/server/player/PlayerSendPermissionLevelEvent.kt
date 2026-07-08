/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.permissions.LevelBasedPermissionSet

public data class PlayerSendPermissionLevelEvent(
    override val player: ServerPlayer,
    val permissions: LevelBasedPermissionSet
): PlayerEvent