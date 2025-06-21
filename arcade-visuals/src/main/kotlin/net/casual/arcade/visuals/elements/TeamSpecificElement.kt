/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.elements

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.scores.PlayerTeam
import org.jetbrains.annotations.ApiStatus.NonExtendable

public interface TeamSpecificElement<E: Any>: PlayerSpecificElement<E> {
    public fun get(server: MinecraftServer, team: PlayerTeam?): E

    @NonExtendable
    override fun get(player: ServerPlayer): E {
        return this.get(player.levelServer, player.team)
    }

    @NonExtendable
    override fun cached(): TeamSpecificElement<E> {
        return Cached(this)
    }

    public companion object {
        public fun <E: Any> cached(element: TeamSpecificElement<E>): TeamSpecificElement<E> {
            return Cached(element)
        }
    }

    private class Cached<E: Any>(private val wrapped: TeamSpecificElement<E>): TeamSpecificElement<E> {
        private val cache = Reference2ObjectOpenHashMap<PlayerTeam?, E>()

        override fun get(server: MinecraftServer, team: PlayerTeam?): E {
            return this.cache.getOrPut(team) {
                this.wrapped.get(server, team)
            }
        }

        override fun tick(server: MinecraftServer) {
            this.cache.clear()
        }
    }
}