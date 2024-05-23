package net.casual.arcade.resources.extensions

import net.casual.arcade.Arcade
import net.casual.arcade.extensions.Extension
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.resources.PackState
import net.casual.arcade.resources.PackStatus
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.jvm.optionals.getOrNull

internal class PlayerPackExtension: Extension {
    internal val futures = HashMap<UUID, MutableList<CompletableFuture<PackStatus>>>()
    private val packs = HashMap<UUID, PackState>()

    internal fun getPackState(uuid: UUID): PackState? {
        return this.packs[uuid]
    }

    internal fun getAllPacks(): MutableCollection<PackState> {
        return this.packs.values
    }

    internal fun addFuture(uuid: UUID): CompletableFuture<PackStatus> {
        val future = CompletableFuture<PackStatus>()
        this.futures.getOrPut(uuid) { ArrayList() }.add(future)
        return future
    }

    internal fun onPackStatus(uuid: UUID, status: PackStatus) {
        if (status == PackStatus.REMOVED) {
            if (this.packs.remove(uuid) != null) {
                Arcade.logger.warn("Client removed resource pack without server telling it to!")
            }
            this.futures.remove(uuid)?.forEach { it.complete(status) }
            return
        }
        val state = this.packs[uuid]
        if (state == null) {
            Arcade.logger.warn("Client is using server resource pack that server is unaware of!?")
            return
        }
        state.setStatus(status)

        if (!status.isLoadingPack()) {
            this.futures.remove(uuid)?.forEach { it.complete(status) }
        }
    }

    internal fun onPushPack(packet: ClientboundResourcePackPushPacket) {
        val info = PackInfo(packet.url, packet.hash, packet.required, packet.prompt.getOrNull(), packet.id)
        val state = PackState(info, PackStatus.WAITING)
        this.packs[info.uuid] = state
    }

    internal fun onPopPack(packet: ClientboundResourcePackPopPacket) {
        val uuid = packet.id
        if (uuid.isEmpty) {
            this.packs.clear()
            return
        }
        this.packs.remove(uuid.get())
    }
}