/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.utils

import net.casual.arcade.pack.PackInfo
import net.casual.arcade.pack.PackState
import net.casual.arcade.pack.PackStatus
import net.casual.arcade.pack.extensions.PlayerPackExtension
import net.casual.arcade.pack.extensions.PlayerPackExtension.Companion.packExtension
import net.casual.arcade.pack.host.HostedPack
import net.casual.arcade.pack.host.HostedPackRef
import net.casual.arcade.utils.coroutine.launch
import net.casual.arcade.utils.player.server
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import java.util.*

public object ResourcePackUtils {
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
        return PlayerPackExtension.getExtension(playerUUID).getPackState(packUUID)
    }

    @JvmStatic
    public fun getPlayerAllPackStates(playerUUID: UUID): Collection<PackState> {
        return PlayerPackExtension.getExtension(playerUUID).getAllPacks()
    }

    @JvmStatic
    public suspend fun awaitPlayerPacks(playerUUID: UUID) {
        PlayerPackExtension.getExtension(playerUUID).awaitPacks()
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
    public suspend fun awaitPacks(players: Iterable<ServerPlayer>) {
        for (player in players) {
            player.awaitPacks()
        }
    }

    @JvmStatic
    public suspend fun ServerPlayer.awaitPacks() {
        awaitPlayerPacks(this.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.sendResourcePack(pack: PackInfo, replace: Boolean = true) {
        this.server.launch { awaitResourcePack(pack, replace) }
    }

    @JvmStatic
    public suspend fun ServerPlayer.awaitResourcePack(pack: PackInfo, replace: Boolean = true): PackStatus {
        val current = this.getPackState(pack)
        if (!replace && current != null) {
            if (current.isLoadingPack()) {
                return current.await()
            }
            if (current.hasLoadedPack()) {
                return PackStatus.SUCCESS
            }
        }

        this.connection.send(pack.toPushPacket(this.connection))
        return this.packExtension.awaitPack(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.removeResourcePack(pack: PackInfo) {
        this.connection.send(pack.toPopPacket())
    }

    @JvmStatic
    public fun ServerPlayer.removeAllResourcePacks() {
        this.connection.send(ClientboundResourcePackPopPacket(Optional.empty()))
    }

    @JvmStatic
    public fun ServerPlayer.flushResourcePacks() {
        this.packExtension.flush(this.connection, this.server)
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
        return PackInfo(this.url, this.hash, required, prompt, this.uuid)
    }

    public fun HostedPackRef.toPackInfo(required: Boolean = false, prompt: Component? = null): PackInfoRef {
        return PackInfoRef(this, required, prompt)
    }

    public class PackInfoRef internal constructor(
        private val ref: HostedPackRef,
        private val required: Boolean,
        private val prompt: Component?
    ) {
        public fun isHosted(): Boolean {
            return this.ref.isHosted()
        }

        public fun getNow(): PackInfo? {
            return this.ref.getNow()?.toInfo()
        }

        public suspend fun await(): PackInfo {
            return this.ref.await().toInfo()
        }

        public fun join(): PackInfo {
            return this.ref.join().toInfo()
        }

        private fun HostedPack.toInfo(): PackInfo {
            return this.toPackInfo(required, prompt)
        }
    }
}