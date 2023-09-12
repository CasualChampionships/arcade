package net.casual.arcade.minigame

import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.ResourcePackUtils
import net.casual.arcade.utils.ResourcePackUtils.sendResourcePack
import net.minecraft.server.level.ServerPlayer

/**
 * This interface is for getting resource pack information
 * for a given minigame.
 *
 * @see Minigame.getResources
 */
public interface MinigameResources {
    /**
     * This gets the default [PackInfo] for a minigame.
     *
     * @return The default pack info.
     */
    public fun getInfo(): PackInfo? {
        return null
    }

    /**
     * This gets the [PackInfo] for a minigame for a
     * given player, this way you may specify the pack that is sent
     * to each player, for example, if players on different teams
     * require a different resource pack.
     *
     * @param player The player that the pack info is for.
     * @return The pack info for the player.
     * @see sendTo
     */
    public fun getInfo(player: ServerPlayer): PackInfo? {
        return this.getInfo()
    }

    public companion object {
        /**
         * This object is the default [MinigameResources].
         * Players will not be sent a resource pack.
         */
        @JvmField
        public val NONE: MinigameResources = object: MinigameResources { }

        /**
         * This object provides an empty implementation of
         * [MinigameResources], which provides a completely empty
         * resource pack.
         *
         * @see ResourcePackUtils.EMPTY_PACK
         */
        @JvmField
        public val EMPTY: MinigameResources = object: MinigameResources {
            override fun getInfo(): PackInfo {
                return ResourcePackUtils.EMPTY_PACK
            }
        }

        /**
         * Tries to send the [MinigameResources] to the player.
         * If the result of [getInfo] is null then the resources cannot
         * be sent to the player, and this method will return false.
         *
         * @param player The player to send the resources to.
         * @return Whether the player was sent the resources.
         */
        @JvmStatic
        public fun MinigameResources.sendTo(player: ServerPlayer): Boolean {
            val info = this.getInfo(player) ?: return false
            player.sendResourcePack(info)
            return true
        }

        /**
         * Tries to send the [MinigameResources] to the given players.
         *
         * @param players The players to send the resources to.
         * @see sendTo
         */
        @JvmStatic
        public fun MinigameResources.sendTo(players: Collection<ServerPlayer>) {
            for (player in players) {
                this.sendTo(player)
            }
        }
    }
}