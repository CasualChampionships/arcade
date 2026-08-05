/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.configuration

import com.mojang.authlib.GameProfile
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.npc.FakePlayer
import net.casual.arcade.npc.network.FakeConnection
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.CommonListenerCookie
import net.minecraft.server.network.ConfigurationTask
import net.minecraft.server.players.NameAndId
import java.util.concurrent.CompletableFuture

internal object FakePlayerConfigurationTasks {
    private val spawnTasks = ArrayList<TaskWithFuture<FakePlayerPrepareSpawnTask, FakePlayer>>()

    fun prepareAndSpawnPlayer(
        server: MinecraftServer,
        profile: GameProfile,
        connection: FakeConnection,
        cookies: CommonListenerCookie,
        constructor: FakePlayerConstructor<*>
    ): CompletableFuture<FakePlayer> {
        val task = FakePlayerPrepareSpawnTask(server, NameAndId(profile), connection, cookies, constructor)
        val future = CompletableFuture<FakePlayer>()
        this.spawnTasks.add(TaskWithFuture(task, future))
        task.start { _ -> }
        return future
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<ServerTickEvent> { this.onServerTick() }
    }

    private fun onServerTick() {
        val ready = ArrayList<TaskWithFuture<FakePlayerPrepareSpawnTask, FakePlayer>>()
        val iterator = this.spawnTasks.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            if (entry.task.tick()) {
                iterator.remove()
                ready.add(entry)
            }
        }

        // This needs to be done *after* due to futures being able to modify spawnTasks on completion
        for ((task, future) in ready) {
            try {
                future.complete(task.spawnPlayer())
            } catch (e: Exception) {
                future.completeExceptionally(e)
            }
        }
    }

    private data class TaskWithFuture<T: ConfigurationTask, S>(val task: T, val future: CompletableFuture<S>)
}