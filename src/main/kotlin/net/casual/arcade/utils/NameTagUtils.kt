package net.casual.arcade.utils

import eu.pb4.polymer.virtualentity.api.ElementHolder
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerPoseEvent
import net.casual.arcade.gui.extensions.PlayerNameTagExtension
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Pose
import org.jetbrains.annotations.ApiStatus.Internal

public object NameTagUtils {
    internal val ServerPlayer.nameTags
        get() = this.getExtension(PlayerNameTagExtension::class.java)

    public fun ElementHolder.isWatching(player: ServerPlayer): Boolean {
        return this.watchingPlayers.contains(player.connection)
    }

    @Internal
    public fun respawn(dead: ServerPlayer, respawned: ServerPlayer) {
        dead.nameTags.respawn(respawned)
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerNameTagExtension(player.connection))
        }
        GlobalEventHandler.register<PlayerPoseEvent> { (player, previous, next) ->
            if (previous == Pose.CROUCHING) {
                player.nameTags.unsneak()
            } else if (next == Pose.CROUCHING) {
                player.nameTags.sneak()
            }
        }
    }
}