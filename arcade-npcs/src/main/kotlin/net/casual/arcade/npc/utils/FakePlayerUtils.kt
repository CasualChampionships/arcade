/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.utils

import net.minecraft.server.level.ServerPlayer

public fun ServerPlayer.isRealPlayer(): Boolean {
    return this::class.java == ServerPlayer::class.java
}