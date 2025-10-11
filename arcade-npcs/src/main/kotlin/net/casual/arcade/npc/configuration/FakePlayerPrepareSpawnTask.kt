/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.configuration

import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.ducks.ReplaceablePlayerConstructor
import net.casual.arcade.npc.mixins.configuration.PrepareSpawnTaskAccessor
import net.casual.arcade.npc.network.FakeConnection
import net.minecraft.network.Connection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.config.PrepareSpawnTask
import net.minecraft.server.players.NameAndId

internal class FakePlayerPrepareSpawnTask(
    server: MinecraftServer,
    nameAndId: NameAndId,
    private val connection: FakeConnection,
    private val cookies: CommonListenerCookie,
    private val constructor: FakePlayerConstructor<*>
): PrepareSpawnTask(server, nameAndId) {
    @Suppress("CAST_NEVER_SUCCEEDS")
    private val access: PrepareSpawnTaskAccessor
        get() = this as PrepareSpawnTaskAccessor

    fun spawnPlayer(): FakePlayer {
        val players = this.access.server.playerList
        if (players.getPlayer(this.access.nameAndId.id) != null) {
            throw IllegalStateException("Duplicate login")
        }
        return super.spawnPlayer(this.connection, this.cookies) as FakePlayer
    }

    override fun tick(): Boolean {
        val finished = super.tick()
        if (finished) {
            assert(this.access.state is Ready)
            val ready = this.access.state as ReplaceablePlayerConstructor
            ready.`arcade$set`(this.constructor)
        }
        return finished
    }

    @Deprecated("Use spawnPlayer() instead", ReplaceWith("this.spawnPlayer()"))
    override fun spawnPlayer(connection: Connection, commonListenerCookie: CommonListenerCookie): FakePlayer {
        return this.spawnPlayer()
    }
}