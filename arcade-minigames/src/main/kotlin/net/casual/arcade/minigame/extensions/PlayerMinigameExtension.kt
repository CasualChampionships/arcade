/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.ListenerRegistry.Companion.register
import net.casual.arcade.events.server.ServerSaveEvent
import net.casual.arcade.events.server.player.PlayerJoinEvent
import net.casual.arcade.events.server.player.PlayerLeaveEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.Minigames
import net.casual.arcade.minigame.utils.MinigameUtils.getMinigame
import net.casual.arcade.minigame.utils.MinigameUtils.minigame
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.PlayerUtils.levelServer
import net.minecraft.core.UUIDUtil
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtAccounter
import net.minecraft.nbt.NbtIo
import net.minecraft.server.level.ServerPlayer
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.createParentDirectories
import kotlin.io.path.isRegularFile
import kotlin.jvm.optionals.getOrNull

internal class PlayerMinigameExtension(
    owner: ServerPlayer
): PlayerExtension(owner) {
    private var minigame: Minigame? = null

    init {
        this.read()
    }

    internal fun getMinigame(): Minigame? {
        return this.minigame
    }

    internal fun setMinigame(minigame: Minigame) {
        this.minigame?.players?.remove(this.player)
        this.minigame = minigame
    }

    internal fun removeMinigame() {
        this.minigame = null
    }

    private fun read() {
        val path = this.getPath()
        if (path.isRegularFile()) {
            try {
                val tag = NbtIo.readCompressed(path, NbtAccounter.unlimitedHeap())
                val uuid = tag.read("minigame", UUIDUtil.CODEC).getOrNull()
                if (uuid != null) {
                    val minigame = Minigames.get(uuid)
                    this.minigame = minigame
                    if (minigame == null) {
                        ArcadeUtils.logger.warn("Player ${this.player.scoreboardName} was part of an old minigame...")
                    }
                }
            } catch (_: IOException) {

            }
        }
    }

    private fun save() {
        val tag = CompoundTag()
        val minigame = this.minigame
        if (minigame != null) {
            tag.store("minigame", UUIDUtil.CODEC, minigame.uuid)
        }
        try {
            NbtIo.writeCompressed(tag, this.getPath().createParentDirectories())
        } catch (_: IOException) {

        }
    }

    private fun getPath(): Path {
        return Minigames.getPath(this.player.levelServer)
            .resolve("players")
            .resolve(this.player.stringUUID + ".nbt")
    }

    companion object {
        fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerExtensionEvent> { event ->
                event.addExtension(::PlayerMinigameExtension)
            }
            GlobalEventHandler.Server.register<PlayerJoinEvent>(Int.MIN_VALUE) { (player) ->
                player.getMinigame()?.players?.add(player)
            }
            GlobalEventHandler.Server.register<ServerSaveEvent> {
                for (player in it.server.playerList.players) {
                    player.minigame.save()
                }
            }
            GlobalEventHandler.Server.register<PlayerLeaveEvent> {
                val extension = it.player.minigame
                extension.save()
                // Prevent any memory leaks
                extension.removeMinigame()
            }
        }
    }
}