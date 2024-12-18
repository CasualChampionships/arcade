/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.elements

import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.level.Level
import org.jetbrains.annotations.ApiStatus.NonExtendable

public fun interface LevelSpecificElement<E: Any>: PlayerSpecificElement<E> {
    public fun get(level: ServerLevel): E

    @NonExtendable
    override fun get(player: ServerPlayer): E {
        return this.get(player.serverLevel())
    }

    @NonExtendable
    override fun cached(): LevelSpecificElement<E> {
        return Cached(this)
    }

    public companion object {
        public fun <E: Any> cached(element: LevelSpecificElement<E>): LevelSpecificElement<E> {
            return Cached(element)
        }
    }

    private class Cached<E: Any>(private val wrapped: LevelSpecificElement<E>): LevelSpecificElement<E> {
        private val cache = Reference2ObjectOpenHashMap<ResourceKey<Level>, E>()

        override fun get(level: ServerLevel): E {
            return this.cache.getOrPut(level.dimension()) {
                this.wrapped.get(level)
            }
        }

        override fun tick(server: MinecraftServer) {
            this.cache.clear()
            this.wrapped.tick(server)
        }
    }
}