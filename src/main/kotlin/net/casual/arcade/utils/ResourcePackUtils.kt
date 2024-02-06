package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.network.ClientboundPacketEvent
import net.casual.arcade.events.network.PackStatusEvent
import net.casual.arcade.events.network.PlayerDisconnectEvent
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.resources.PackStatus
import net.casual.arcade.resources.PlayerPackExtension
import net.minecraft.network.protocol.common.ClientboundResourcePackPopPacket
import net.minecraft.network.protocol.common.ClientboundResourcePackPushPacket
import net.minecraft.server.level.ServerPlayer
import java.util.*
import java.util.concurrent.CompletableFuture

public object ResourcePackUtils {
    private val universe = HashMap<UUID, PlayerPackExtension>()

    private val ServerPlayer.resourcePacks
        get() = getExtension(this.uuid)

    @JvmStatic
    public fun ServerPlayer.sendResourcePack(pack: PackInfo): CompletableFuture<PackStatus> {
        this.connection.send(ClientboundResourcePackPushPacket(
            pack.uuid, pack.url, pack.hash, pack.required, pack.prompt
        ))
        return this.resourcePacks.addFuture(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.removeResourcePack(pack: PackInfo): CompletableFuture<PackStatus> {
        this.connection.send(ClientboundResourcePackPopPacket(Optional.of(pack.uuid)))
        return this.resourcePacks.addFuture(pack.uuid)
    }

    @JvmStatic
    public fun ServerPlayer.removeAllResourcePacks() {
        this.connection.send(ClientboundResourcePackPopPacket(Optional.empty()))
    }

    private fun getExtension(uuid: UUID): PlayerPackExtension {
        return this.universe.getOrPut(uuid) { PlayerPackExtension() }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerDisconnectEvent> { (_, profile) ->
            this.universe.remove(profile.id)
        }
        GlobalEventHandler.register<ClientboundPacketEvent> { (_, profile, packet) ->
            // This may be off thread
            if (packet is ClientboundResourcePackPushPacket) {
                this.getExtension(profile.id).onPushPack(packet)
            } else if (packet is ClientboundResourcePackPopPacket) {
                this.getExtension(profile.id).onPopPack(packet)
            }
        }
        GlobalEventHandler.register<PackStatusEvent> { (_, profile, uuid, status) ->
            this.getExtension(profile.id).onPackStatus(uuid, status)
        }
    }
}