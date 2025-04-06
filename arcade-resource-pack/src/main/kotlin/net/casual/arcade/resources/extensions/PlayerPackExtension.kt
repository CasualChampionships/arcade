/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.extensions

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.extensions.Extension
import net.casual.arcade.host.data.ResolvablePackURL
import net.casual.arcade.resources.event.ClientPacksSuccessEvent
import net.casual.arcade.resources.event.PlayerPacksSuccessEvent
import net.casual.arcade.resources.pack.PackInfo
import net.casual.arcade.resources.pack.PackState
import net.casual.arcade.resources.pack.PackStatus
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.PlayerUtils.player
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.server.MinecraftServer
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

internal class PlayerPackExtension(private val uuid: UUID): Extension {
    internal val futures = Object2ObjectOpenHashMap<UUID, CompletableFuture<PackStatus>>()
    private val packs = Object2ObjectOpenHashMap<UUID, PackState>()

    internal var allLoadedFuture = CompletableFuture<Void>()

    internal fun getPackState(uuid: UUID): PackState? {
        return this.packs[uuid]
    }

    internal fun getAllPacks(): Collection<PackState> {
        return this.packs.values
    }

    internal fun addFuture(uuid: UUID): CompletableFuture<PackStatus> {
        if (this.allLoadedFuture.isDone) {
            this.allLoadedFuture = CompletableFuture()
        }
        return this.futures.getOrPut(uuid) { CompletableFuture() }
    }

    internal fun onPackStatus(server: MinecraftServer, uuid: UUID, status: PackStatus) {
        if (status == PackStatus.REMOVED) {
            if (this.packs.remove(uuid) != null) {
                ArcadeUtils.logger.warn("Client removed resource pack without server telling it to!")
            }
            this.futures.remove(uuid)?.complete(status)
            this.checkAllFutures(server)
            return
        }
        val state = this.packs[uuid]
        if (state == null) {
            ArcadeUtils.logger.warn("Client is using server resource pack that server is unaware of!?")
            return
        }
        state.setStatus(status)

        if (!status.isLoadingPack()) {
            this.futures[uuid]?.complete(status)
            this.checkAllFutures(server)
        }
    }

    internal fun onPushPack(packet: ClientboundResourcePackPushPacket) {
        val url = ResolvablePackURL.from(packet.url)
        val info = PackInfo(url, packet.hash, packet.required, packet.prompt.getOrNull(), packet.id)
        val state = PackState(info, PackStatus.WAITING)
        this.packs[info.uuid] = state

        this.addFuture(packet.id)
    }

    internal fun onPopPack(packet: ClientboundResourcePackPopPacket) {
        val uuid = packet.id
        if (uuid.isEmpty) {
            this.packs.clear()
            return
        }
        this.packs.remove(uuid.get())
    }

    private fun checkAllFutures(server: MinecraftServer) {
        for (future in this.futures.values) {
            if (!future.isDone) {
                return
            }
        }
        this.allLoadedFuture.complete(null)
        GlobalEventHandler.Server.broadcast(ClientPacksSuccessEvent(this.uuid, this.getAllPacks()))
        val player = server.player(this.uuid)
        if (player != null) {
            GlobalEventHandler.Server.broadcast(PlayerPacksSuccessEvent(player, this.getAllPacks()))
        }
    }
}