/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.Event
import net.minecraft.server.MinecraftServer

/**
 * This event is fired whenever the player is disconnected from
 * the server, this includes when the client is in the configuration
 * phase. Therefore, we cannot guarantee that the player is
 * actually in our world, if you are looking for an event where the
 * player has actually joined the game, see [PlayerLeaveEvent].
 */
public data class PlayerDisconnectEvent(
    val server: MinecraftServer,
    val profile: GameProfile
): Event