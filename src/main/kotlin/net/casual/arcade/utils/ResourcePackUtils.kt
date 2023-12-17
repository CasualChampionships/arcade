package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.network.PackStatusEvent
import net.casual.arcade.events.network.PlayerDisconnectEvent
import net.casual.arcade.events.network.PlayerLoginEvent
import net.casual.arcade.events.player.PlayerClientboundPacketEvent
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
        get() = universe[this.uuid]!!

    @JvmStatic
    public fun ServerPlayer.sendResourcePack(pack: PackInfo): CompletableFuture<PackStatus> {
        val future = CompletableFuture<PackStatus>()
        this.connection.send(ClientboundResourcePackPushPacket(
            pack.uuid, pack.url, pack.hash, pack.required, pack.prompt
        ))
        this.resourcePacks.futures[pack.uuid] = future
        return future
    }

    @JvmStatic
    public fun ServerPlayer.removeResourcePack(pack: PackInfo): CompletableFuture<PackStatus> {
        val future = CompletableFuture<PackStatus>()
        this.connection.send(ClientboundResourcePackPopPacket(Optional.of(pack.uuid)))
        this.resourcePacks.futures[pack.uuid] = future
        return future
    }

    @JvmStatic
    public fun ServerPlayer.removeAllResourcePacks() {
        this.connection.send(ClientboundResourcePackPopPacket(Optional.empty()))
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerLoginEvent> { (_, profile) ->
            this.universe[profile.id] = PlayerPackExtension()
        }
        GlobalEventHandler.register<PlayerDisconnectEvent> { (_, profile) ->
            this.universe.remove(profile.id)
        }
        GlobalEventHandler.register<PlayerClientboundPacketEvent> { (player, packet) ->
            if (packet is ClientboundResourcePackPushPacket) {
                player.resourcePacks.onPushPack(packet)
            } else if (packet is ClientboundResourcePackPopPacket) {
                player.resourcePacks.onPopPack(packet)
            }
        }
        GlobalEventHandler.register<PackStatusEvent> { (_, profile, uuid, status) ->
            this.universe[profile.id]?.onPackStatus(uuid, status)
        }
    }
}