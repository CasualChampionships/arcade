package net.casual.arcade.minigame

import net.minecraft.server.MinecraftServer.ServerResourcePackInfo
import net.minecraft.server.level.ServerPlayer

/**
 * This interface is for getting resource pack information
 * for a given minigame.
 *
 * @see Minigame.getResources
 */
interface MinigameResources {
    /**
     * This gets the default [ServerResourcePackInfo] for a minigame.
     *
     * @return The default pack info.
     */
    fun getInfo(): ServerResourcePackInfo? {
        return null
    }

    /**
     * This gets the [ServerResourcePackInfo] for a minigame for a
     * given player, this way you may specify the pack that is sent
     * to each player, for example, if players on different teams
     * require a different resource pack.
     *
     * @param player The player that the pack info is for.
     * @return The pack info for the player.
     * @see sendTo
     */
    fun getInfo(player: ServerPlayer): ServerResourcePackInfo? {
        return this.getInfo()
    }

    companion object {
        /**
         * This object is the default [MinigameResources].
         * Players will not be sent a resource pack.
         */
        @JvmField
        val NONE = object: MinigameResources { }

        /**
         * Tries to send the [MinigameResources] to the player.
         * If the result of [getInfo] is null then the resources cannot
         * be sent to the player, and this method will return false.
         *
         * @param player The player to send the resources to.
         * @return Whether the player was sent the resources.
         */
        @JvmStatic
        fun MinigameResources.sendTo(player: ServerPlayer): Boolean {
            val info = this.getInfo(player) ?: return false
            player.sendTexturePack(info.url, info.hash, info.isRequired, info.prompt)
            return true
        }
    }
}