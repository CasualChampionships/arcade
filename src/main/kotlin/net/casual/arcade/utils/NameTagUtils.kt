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

object NameTagUtils {
    internal val ServerPlayer.nameTags
        get() = this.getExtension(PlayerNameTagExtension::class.java)

    fun ElementHolder.isWatching(player: ServerPlayer): Boolean {
        return this.watchingPlayers.contains(player.connection)
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerNameTagExtension(player))
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