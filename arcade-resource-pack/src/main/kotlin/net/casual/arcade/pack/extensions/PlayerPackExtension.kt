/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.extensions

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap
import kotlinx.coroutines.CompletableDeferred
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerTickEvent
import net.casual.arcade.events.server.network.ClientboundPacketEvent
import net.casual.arcade.events.server.player.PlayerDimensionChangeEvent
import net.casual.arcade.events.server.player.PlayerDisconnectEvent
import net.casual.arcade.events.utils.register
import net.casual.arcade.extensions.Extension
import net.casual.arcade.pack.PackInfo
import net.casual.arcade.pack.PackState
import net.casual.arcade.pack.PackStatus
import net.casual.arcade.pack.event.ClientPacksSuccessEvent
import net.casual.arcade.pack.event.PackStatusEvent
import net.casual.arcade.pack.event.PlayerPacksSuccessEvent
import net.casual.arcade.pack.utils.ResourcePackUtils.toPushPacket
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.network.ResolvableURL
import net.casual.arcade.utils.server.player
import net.minecraft.network.protocol.Packet
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.game.ClientGamePacketListener
import net.minecraft.network.protocol.game.ClientboundBundlePacket
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerCommonPacketListenerImpl
import net.minecraft.server.network.ServerGamePacketListenerImpl
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.optionals.getOrNull

// This tracks the packs which the player has been sent and the state of each pack
// it also buffers packs so they are batched to reduce the number of reloads on the client
internal class PlayerPackExtension(private val uuid: UUID): Extension {
    private val packs = Object2ObjectLinkedOpenHashMap<UUID, PackState>()
    private var queued: Object2ObjectLinkedOpenHashMap<UUID, PackInfo>? = null
    private var modified = false
    private var buffered = 0

    private val sending = ScopedValue.newInstance<Boolean>()

    private var flushed = CompletableDeferred<Unit>()
    private var settled = CompletableDeferred(Unit)

    internal fun getPackState(uuid: UUID): PackState? {
        return this.packs[uuid]
    }

    internal fun getAllPacks(): Collection<PackState> {
        return this.packs.values
    }

    internal suspend fun awaitPacks() {
        this.settled.await()
    }

    internal suspend fun awaitPack(uuid: UUID): PackStatus {
        this.flushed.await()
        return this.packs[uuid]?.await() ?: PackStatus.REMOVED
    }

    internal fun onPushPack(packet: ClientboundResourcePackPushPacket): Boolean {
        if (this.sending.orElse(false)) {
            return false
        }
        val url = ResolvableURL.from(packet.url)
        val info = PackInfo(url, packet.hash, packet.required, packet.prompt.getOrNull(), packet.id)
        this.queue().putAndMoveToLast(info.uuid, info)
        this.modified = true
        this.unsettle()
        return true
    }

    internal fun onPopPack(packet: ClientboundResourcePackPopPacket): Boolean {
        if (this.sending.orElse(false)) {
            return false
        }
        val uuid = packet.id.getOrNull()
        if (uuid == null) {
            this.queue().clear()
        } else {
            this.queue().remove(uuid)
        }
        this.modified = true
        return true
    }

    internal fun tick(connection: ServerCommonPacketListenerImpl, server: MinecraftServer) {
        if (this.queued == null) {
            return
        }
        if (this.modified && this.buffered < MAX_BUFFERED_TICKS) {
            this.modified = false
            this.buffered++
            return
        }
        this.flush(connection, server)
    }

    internal fun flush(connection: ServerCommonPacketListenerImpl, server: MinecraftServer) {
        val desired = this.queued ?: return
        this.queued = null
        this.modified = false
        this.buffered = 0

        val current = ArrayList(this.packs.keys)
        val retained = this.countRetained(current, desired.values)
        val popped = current.filterTo(LinkedHashSet()) { !desired.containsKey(it) }
        val pushed = desired.values.drop(retained)

        val packets = this.createPackets(connection, current, popped, pushed)
        for (uuid in popped) {
            this.packs.remove(uuid)?.setStatus(PackStatus.REMOVED)
        }
        for (pack in pushed) {
            this.packs.putAndMoveToLast(pack.uuid, PackState(pack, PackStatus.WAITING))?.setStatus(PackStatus.REMOVED)
        }
        this.send(connection, packets)

        val flushed = this.flushed
        this.flushed = CompletableDeferred()
        flushed.complete(Unit)

        this.checkSettled(server)
    }

    internal fun resend(player: ServerPlayer) {
        val packets = this.packs.values
            .filter(PackState::isWaitingForResponse)
            .map { it.info.toPushPacket(player.connection) }
        this.send(player.connection, packets)
    }

    internal fun onPackStatus(server: MinecraftServer, uuid: UUID, status: PackStatus) {
        if (status == PackStatus.REMOVED) {
            val removed = this.packs.remove(uuid)
            if (removed != null) {
                removed.setStatus(status)
                ArcadeUtils.logger.warn("Client removed resource pack without server telling it to!")
            }
            this.checkSettled(server)
            return
        }
        val state = this.packs[uuid]
        if (state == null) {
            ArcadeUtils.logger.warn("Client is using server resource pack that server is unaware of!?")
            return
        }
        state.setStatus(status)

        if (!status.isLoadingPack()) {
            this.checkSettled(server)
        }
    }

    internal fun onDisconnect() {
        this.queued = null
        this.modified = false
        this.buffered = 0
        for (state in this.packs.values) {
            state.setStatus(PackStatus.REMOVED)
        }
        this.packs.clear()
        this.flushed.complete(Unit)
        this.settled.complete(Unit)
    }

    private fun queue(): Object2ObjectLinkedOpenHashMap<UUID, PackInfo> {
        var queued = this.queued
        if (queued == null) {
            queued = Object2ObjectLinkedOpenHashMap()
            for (state in this.packs.values) {
                queued.putAndMoveToLast(state.info.uuid, state.info)
            }
            this.queued = queued
        }
        return queued
    }

    private fun countRetained(current: List<UUID>, desired: Collection<PackInfo>): Int {
        var index = 0
        var retained = 0
        for (pack in desired) {
            val found = current.subList(index, current.size).indexOf(pack.uuid)
            if (found == -1) {
                break
            }
            val state = this.packs[pack.uuid]!!
            if (state.info != pack || state.hasDeclinedPack() || state.hasFailedToLoadPack()) {
                break
            }
            index += found + 1
            retained++
        }
        return retained
    }

    private fun createPackets(
        connection: ServerCommonPacketListenerImpl,
        current: List<UUID>,
        popped: Set<UUID>,
        pushed: List<PackInfo>
    ): List<Packet<in ClientGamePacketListener>> {
        val packets = ArrayList<Packet<in ClientGamePacketListener>>()
        if (popped.size == current.size && popped.isNotEmpty()) {
            packets.add(ClientboundResourcePackPopPacket(Optional.empty()))
        } else {
            for (uuid in popped) {
                packets.add(ClientboundResourcePackPopPacket(Optional.of(uuid)))
            }
        }
        for (pack in pushed) {
            packets.add(pack.toPushPacket(connection))
        }
        return packets
    }

    private fun send(connection: ServerCommonPacketListenerImpl, packets: List<Packet<in ClientGamePacketListener>>) {
        if (packets.isEmpty()) {
            return
        }
        ScopedValue.where(this.sending, true).run {
            if (packets.size > 1 && connection is ServerGamePacketListenerImpl) {
                connection.send(ClientboundBundlePacket(packets))
                return@run
            }
            for (packet in packets) {
                connection.send(packet)
            }
        }
    }

    private fun unsettle() {
        if (this.settled.isCompleted) {
            this.settled = CompletableDeferred()
        }
    }

    private fun checkSettled(server: MinecraftServer) {
        if (this.settled.isCompleted || this.queued != null) {
            return
        }
        for (state in this.packs.values) {
            if (state.isLoadingPack()) {
                return
            }
        }
        this.settled.complete(Unit)

        GlobalEventHandler.Server.broadcast(ClientPacksSuccessEvent(this.uuid, this.getAllPacks()))
        val player = server.player(this.uuid)
        if (player != null) {
            GlobalEventHandler.Server.broadcast(PlayerPacksSuccessEvent(player, this.getAllPacks()))
        }
    }

    companion object {
        private const val MAX_BUFFERED_TICKS = 5

        // May be accessed off the main thread.
        // This is implemented like this since we cannot use PlayerExtensions.
        // Packs may be sent before the player has spawned in the world.
        private val universe = ConcurrentHashMap<UUID, PlayerPackExtension>()

        val ServerPlayer.packExtension: PlayerPackExtension
            get() = getExtension(this.uuid)

        fun getExtension(uuid: UUID): PlayerPackExtension {
            return this.universe.getOrPut(uuid) { PlayerPackExtension(uuid) }
        }

        internal fun registerEvents() {
            GlobalEventHandler.Server.register<PlayerDisconnectEvent> { (_, profile) ->
                this.universe.remove(profile.id)?.onDisconnect()
            }
            GlobalEventHandler.Server.register<ClientboundPacketEvent> { event ->
                val buffered = when (val packet = event.packet) {
                    is ClientboundResourcePackPushPacket -> this.getExtension(event.owner.id).onPushPack(packet)
                    is ClientboundResourcePackPopPacket -> this.getExtension(event.owner.id).onPopPack(packet)
                    else -> false
                }
                if (buffered) {
                    event.cancel()
                }
            }
            GlobalEventHandler.Server.register<PackStatusEvent> { (server, profile, uuid, status) ->
                this.getExtension(profile.id).onPackStatus(server, uuid, status)
            }
            GlobalEventHandler.Server.register<ServerTickEvent>(phase = ServerTickEvent.PHASE_POST) { (server) ->
                for (connection in ArrayList(server.connection.connections)) {
                    val listener = connection.packetListener
                    if (listener is ServerCommonPacketListenerImpl) {
                        this.universe[listener.owner.id]?.tick(listener, server)
                    }
                }
            }
            // This is done because the level loading screen interrupts the pack load
            GlobalEventHandler.Server.register<PlayerDimensionChangeEvent> { (player) ->
                player.packExtension.resend(player)
            }
        }
    }
}
