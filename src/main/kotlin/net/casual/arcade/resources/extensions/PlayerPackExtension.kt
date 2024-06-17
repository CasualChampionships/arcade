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
    internal val futures = HashMap<UUID, CompletableFuture<PackStatus>>()
    private val packs = HashMap<UUID, PackState>()

    internal var allLoadedFuture = CompletableFuture<Void>()

    internal fun getPackState(uuid: UUID): PackState? {
        return this.packs[uuid]
    }

    internal fun getAllPacks(): MutableCollection<PackState> {
        return this.packs.values
    }

    internal fun addFuture(uuid: UUID): CompletableFuture<PackStatus> {
        if (this.allLoadedFuture.isDone) {
            this.allLoadedFuture = CompletableFuture()
        }
        return this.futures.getOrPut(uuid) { CompletableFuture() }
    }

    internal fun onPackStatus(uuid: UUID, status: PackStatus) {
        if (status == PackStatus.REMOVED) {
            if (this.packs.remove(uuid) != null) {
                Arcade.logger.warn("Client removed resource pack without server telling it to!")
            }
            this.futures.remove(uuid)?.complete(status)
            this.checkAllFutures()
            return
        }
        val state = this.packs[uuid]
        if (state == null) {
            Arcade.logger.warn("Client is using server resource pack that server is unaware of!?")
            return
        }
        state.setStatus(status)

        if (!status.isLoadingPack()) {
            this.futures[uuid]?.complete(status)
            this.checkAllFutures()
        }
    }

    internal fun onPushPack(packet: ClientboundResourcePackPushPacket) {
        val info = PackInfo(packet.url, packet.hash, packet.required, packet.prompt.getOrNull(), packet.id)
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

    private fun checkAllFutures() {
        for (future in this.futures.values) {
            if (!future.isDone) {
                return
            }
        }
        this.allLoadedFuture.complete(null)
    }
}