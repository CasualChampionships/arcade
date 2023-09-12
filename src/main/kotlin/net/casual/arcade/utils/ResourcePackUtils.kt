package net.casual.arcade.utils

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerClientboundPacketEvent
import net.casual.arcade.events.player.PlayerCreatedEvent
import net.casual.arcade.events.player.PlayerPackStatusEvent
import net.casual.arcade.minigame.MinigameResources.Companion.EMPTY
import net.casual.arcade.resources.PackInfo
import net.casual.arcade.resources.PackState
import net.casual.arcade.resources.PackStatus.Companion.toPackStatus
import net.casual.arcade.resources.PlayerPackExtension
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.casual.arcade.utils.PlayerUtils.getExtension
import net.minecraft.network.protocol.game.ClientboundResourcePackPacket
import net.minecraft.server.level.ServerPlayer

public object ResourcePackUtils {
    private val ServerPlayer.resourcePacks
        get() = this.getExtension(PlayerPackExtension::class.java)

    /**
     * This [PackInfo] holds a completely empty resource pack.
     *
     * This may be useful for resetting the client's resource pack.
     *
     * @see EMPTY
     */
    @JvmField
    public val EMPTY_PACK: PackInfo = PackInfo(
        url = "https://download.mc-packs.net/pack/8694214da5d1b2adac38971828e07b20e33d3e24.zip",
        hash = "8694214da5d1b2adac38971828e07b20e33d3e24",
        required = false,
        prompt = null
    )

    @JvmStatic
    public fun ServerPlayer.getPreviousResourcePack(): PackInfo? {
        return this.resourcePacks.previous
    }

    @JvmStatic
    public fun ServerPlayer.getResourcePackState(): PackState? {
        val current = this.resourcePacks.current ?: return null
        return PackState(current, this.resourcePacks.status)
    }

    @JvmStatic
    public fun ServerPlayer.resendLastResourcePack() {
        val state = this.getResourcePackState() ?: return
        this.sendResourcePack(state.pack)
    }

    @JvmStatic
    public fun ServerPlayer.sendResourcePack(pack: PackInfo) {
        this.sendTexturePack(pack.url, pack.hash, pack.required, pack.prompt)
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<PlayerCreatedEvent> { (player) ->
            player.addExtension(PlayerPackExtension())
        }
        GlobalEventHandler.register<PlayerClientboundPacketEvent> { (player, packet) ->
            if (packet is ClientboundResourcePackPacket) {
                player.resourcePacks.onSentPack(packet)
            }
        }
        GlobalEventHandler.register<PlayerPackStatusEvent> { (player, status) ->
            player.resourcePacks.status = status.toPackStatus()
        }
    }
}