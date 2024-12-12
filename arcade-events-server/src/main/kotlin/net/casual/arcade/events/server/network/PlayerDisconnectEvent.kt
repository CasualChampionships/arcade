package net.casual.arcade.events.server.network

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.common.Event
import net.casual.arcade.events.server.player.PlayerLeaveEvent
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