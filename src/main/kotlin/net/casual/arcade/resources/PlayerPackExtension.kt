package net.casual.arcade.resources

import net.casual.arcade.Arcade
import net.casual.arcade.events.network.PackStatusEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.resources.PackStatus.Companion.toPackStatus
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.HashSet

internal class PlayerPackExtension: Extension {
    internal val futures = HashMap<UUID, CompletableFuture<PackStatus>>()
    private val packs = HashMap<UUID, PackState>()

    internal fun onPackStatus(uuid: UUID, status: PackStatus) {
        if (status == PackStatus.REMOVED) {
            if (this.packs.remove(uuid) != null) {
                Arcade.logger.warn("Client removed resource pack without server telling it to!")
            }
            this.futures.remove(uuid)?.complete(status)
            return
        }
        val state = this.packs[uuid]
        if (state == null) {
            Arcade.logger.warn("Client is using server resource pack that server is unaware of!?")
            return
        }
        state.setStatus(status)

        this.futures.remove(uuid)?.complete(status)
    }

    internal fun onPushPack(packet: ClientboundResourcePackPushPacket) {
        val info = PackInfo(packet.url, packet.hash, packet.required, packet.prompt, packet.id)
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