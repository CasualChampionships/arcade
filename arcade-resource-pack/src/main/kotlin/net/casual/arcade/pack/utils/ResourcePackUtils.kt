/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.network.ClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.server.player.PlayerDisconnectEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.pack.PackInfo
import net.casual.arcade.pack.PackState
import net.casual.arcade.pack.PackStatus
import net.casual.arcade.pack.event.PackStatusEvent
import net.casual.arcade.pack.extensions.PlayerPackExtension
import net.casual.arcade.pack.host.HostedPack
import net.casual.arcade.pack.host.PackHost
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KProperty

public object ResourcePackUtils {
    // May be accessed off the main thread.
    // This is implemented like this since we cannot use PlayerExtensions.
    // Packs may be sent before the player has spawned in the world.
    private val universe = ConcurrentHashMap<UUID, PlayerPackExtension>()

    private val ServerPlayer.resourcePacks
        get() = getExtension(this.uuid)

    @JvmStatic
    public fun PackInfo.toPushPacket(connection: ServerCommonPacketListenerImpl): ClientboundResourcePackPushPacket {
        return ClientboundResourcePackPushPacket(
            this.uuid, this.url.resolve(connection), this.hash, this.required, Optional.ofNullable(this.prompt)
        )
    }

    @JvmStatic
    public fun PackInfo.toPushPacket(): ClientboundResourcePackPushPacket {
        return ClientboundResourcePackPushPacket(
            this.uuid, this.url.resolve(), this.hash, this.required, Optional.ofNullable(this.prompt)
        )
    }

    @JvmStatic
    public fun PackInfo.toPopPacket(): ClientboundResourcePackPopPacket {
        return ClientboundResourcePackPopPacket(Optional.of(this.uuid))
    }

    @JvmStatic
    public fun getPlayerPackState(playerUUID: UUID, packUUID: UUID): PackState? {
        return this.getExtension(playerUUID).getPackState(packUUID)
    }

    @JvmStatic
    public fun getPlayerAllPackStates(playerUUID: UUID): Collection<PackState> {
        return this.getExtension(playerUUID).getAllPacks()
    }

    @JvmStatic
    public fun getPlayerPackLoadingFuture(playerUUID: UUID): CompletableFuture<Void> {
        return this.getExtension(playerUUID).allLoadedFuture
    }

    @JvmStatic
    public fun ServerPlayer.getPackState(uuid: UUID): PackState? {
        return getPlayerPackState(this.uuid, uuid)
    }

    @JvmStatic
    public fun ServerPlayer.getPackState(pack: PackInfo): PackState? {
        return this.getPackState(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.hasBeenSentPack(pack: PackInfo): Boolean {
        return this.getPackState(pack) != null
    }

    @JvmStatic
    public fun ServerPlayer.getAllPackStates(): Collection<PackState> {
        return getPlayerAllPackStates(this.uuid)
    }

    @JvmStatic
    public fun afterPacksLoad(players: Iterable<ServerPlayer>, block: () -> Unit) {
        getPackLoadingFuture(players).thenRun(block)
    }

    @JvmStatic
    public fun getPackLoadingFuture(players: Iterable<ServerPlayer>): CompletableFuture<Void> {
        return CompletableFuture.allOf(*players.map { it.getPackLoadingFuture() }.toTypedArray())
    }

    @JvmStatic
    public fun ServerPlayer.afterPacksLoad(block: () -> Unit) {
        this.getPackLoadingFuture().thenRun(block)
    }

    @JvmStatic
    public fun ServerPlayer.getPackLoadingFuture(): CompletableFuture<Void> {
        return getPlayerPackLoadingFuture(this.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.sendResourcePack(pack: PackInfo, replace: Boolean = true): CompletableFuture<PackStatus> {
        val current = this.getPackState(pack)
        if (!replace && current != null) {
            if (current.isLoadingPack()) {
                return this.resourcePacks.addFuture(pack.uuid)
            }
            if (current.hasLoadedPack()) {
                return CompletableFuture.completedFuture(PackStatus.SUCCESS)
            }
        }

        this.connection.send(pack.toPushPacket(this.connection))
        return this.resourcePacks.addFuture(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.removeResourcePack(pack: PackInfo): CompletableFuture<PackStatus> {
        this.connection.send(pack.toPopPacket())
        return this.resourcePacks.addFuture(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.removeAllResourcePacks() {
        this.connection.send(ClientboundResourcePackPopPacket(Optional.empty()))
    }

    /**
     * This converts the [HostedPack] to [PackInfo] to be able
     * to be sent to players on the server.
     *
     * @param required Whether the pack should be required for the player.
     * @param prompt The prompt given to the player.
     * @return The pack info.
     * @see PackInfo
     */
    public fun HostedPack.toPackInfo(required: Boolean = false, prompt: Component? = null): PackInfo {
        return PackInfo(this.url, this.hash, required, prompt)
    }

    public fun PackHost.HostedPackRef.toPackInfo(required: Boolean = false, prompt: Component? = null): PackInfoRef {
        return PackInfoRef(this, required, prompt)
    }

    private fun getExtension(uuid: UUID): PlayerPackExtension {
        return universe.getOrPut(uuid) { PlayerPackExtension(uuid) }
    }

    public class PackInfoRef(
        ref: PackHost.HostedPackRef,
        private val required: Boolean,
        private val prompt: Component?
    ) {
        private val hosted by ref

        public operator fun getValue(any: Any?, property: KProperty<*>): PackInfo {
            return this.hosted.toPackInfo(this.required, this.prompt)
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.Server.register<PlayerDisconnectEvent> { (_, profile) ->
            universe.remove(profile.id)
        }
        GlobalEventHandler.Server.register<ClientboundPacketEvent> { (_, profile, packet) ->
            // This may be off thread
            if (packet is ClientboundResourcePackPushPacket) {
                getExtension(profile.id).onPushPack(packet)
            } else if (packet is ClientboundResourcePackPopPacket) {
                getExtension(profile.id).onPopPack(packet)
            }
        }
        GlobalEventHandler.Server.register<PackStatusEvent> { (server, profile, uuid, status) ->
            getExtension(profile.id).onPackStatus(server, uuid, status)
        }
        GlobalEventHandler.Server.register<PlayerDimensionChangeEvent> { (player) ->
            for (pack in getExtension(player.uuid).getAllPacks()) {
                if (pack.isWaitingForResponse()) {
                    player.sendResourcePack(pack.info, true)
                }
            }
        }
    }
}