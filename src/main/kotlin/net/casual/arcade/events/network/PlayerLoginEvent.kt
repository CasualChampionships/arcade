package net.casual.arcade.events.network

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.core.Event
import net.casual.arcade.events.player.PlayerJoinEvent
import net.minecraft.server.MinecraftServer

/**
 * This event is fired when the server has completed the
 * handshake with the client just before the client is configured.
 * This event happens well before the player actually
 * spawns in the world, if that is the event you are looking for,
 * see [PlayerJoinEvent].
 */
public data class PlayerLoginEvent(
    val server: MinecraftServer,
    val profile: GameProfile
): Event