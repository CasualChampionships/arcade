/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.utils

import it.unimi.dsi.fastutil.objects.ObjectLinkedOpenHashSet
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.utils.ResourcePackUtils.removeResourcePack
import net.casual.arcade.resources.utils.ResourcePackUtils.sendResourcePack
import net.minecraft.server.level.ServerPlayer

/**
 * This interface is for getting resource pack information
 * for a given minigame.
 *
 * @see Minigame.resources
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

    public class MultiMinigameResources: MinigameResources {
        private val resources = ObjectLinkedOpenHashSet<MinigameResources>()

        public fun add(resources: MinigameResources): Boolean {
            return this.resources.add(resources)
        }

        public fun remove(resources: MinigameResources): Boolean {
            return this.resources.remove(resources)
        }

        override fun getPacks(): Collection<PackInfo> {
            return this.resources.flatMap { it.getPacks() }
        }

        override fun getPacks(player: ServerPlayer): Collection<PackInfo> {
            return this.resources.flatMap { it.getPacks(player) }
        }
    }

    public companion object {
        /**
         * This object is the default [MinigameResources].
         * Players will not be sent a resource pack.
         */
        @JvmField
        public val NONE: MinigameResources = object: MinigameResources { }

        /**
         * Creates an instance of [MinigameResources] from a list of [PackInfo].
         *
         * @param packs The list of packs.
         */
        @JvmStatic
        public fun of(vararg packs: PackInfo): MinigameResources {
            val collection = packs.toList()
            return object: MinigameResources {
                override fun getPacks(): Collection<PackInfo> {
                    return collection
                }
            }
        }

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
         * @param player The player to remove the resources from.
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
        public fun MinigameResources.sendTo(players: Iterable<ServerPlayer>) {
            for (player in players) {
                this.sendTo(player)
            }
        }

        /**
         * This pops all the resource packs sent to the given players.
         *
         * @param players The players to remove the resources from.
         */
        @JvmStatic
        public fun MinigameResources.removeFrom(players: Iterable<ServerPlayer>) {
            for (player in players) {
                this.removeFrom(player)
            }
        }
    }
}