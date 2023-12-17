package net.casual.arcade.minigame

import net.casual.arcade.resources.PackInfo
import net.casual.arcade.utils.ResourcePackUtils.removeResourcePack
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
     * This gets the default [PackInfo]s for a minigame.
     *
     * @return The default pack infos.
     */
    public fun getPacks(): Collection<PackInfo> {
        return listOf()
    }

    /**
     * This gets the [PackInfo]s for a minigame for a
     * given player, this way you may specify the packs that are sent
     * to each player, for example, if players on different teams
     * require different resource packs.
     *
     * @param player The player that the pack info is for.
     * @return The pack infos for the player.
     * @see sendTo
     */
    public fun getPacks(player: ServerPlayer): Collection<PackInfo> {
        return this.getPacks()
    }

    public companion object {
        /**
         * This object is the default [MinigameResources].
         * Players will not be sent a resource pack.
         */
        @JvmField
        public val NONE: MinigameResources = object: MinigameResources { }

        /**
         * Tries to send the [MinigameResources] to the player.
         *
         * @param player The player to send the resources to.
         */
        @JvmStatic
        public fun MinigameResources.sendTo(player: ServerPlayer) {
            val packs = this.getPacks(player)
            for (pack in packs) {
                player.sendResourcePack(pack)
            }
        }

        /**
         * This pops all the resource packs sent to a player.
         *
         * @param player The player to send the resources to.
         */
        @JvmStatic
        public fun MinigameResources.removeFrom(player: ServerPlayer) {
            val packs = this.getPacks(player)
            for (pack in packs) {
                player.removeResourcePack(pack)
            }
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