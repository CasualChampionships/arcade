/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.player

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.MissingExecutorEvent
import net.minecraft.server.MinecraftServer

/**
 * This event is fired when the server has completed the
 * handshake with the client just before the client is configured.
 * This event happens well before the player actually
 * spawns in the world, if that is the event you are looking for,
 * see [PlayerJoinEvent].
 */
public data class PlayerLoginEvent(
    /**
     * The [MinecraftServer] instance.
     */
    val server: MinecraftServer,
    /**
     * The profile of the player that is logging in.
     */
    val profile: GameProfile
): MissingExecutorEvent