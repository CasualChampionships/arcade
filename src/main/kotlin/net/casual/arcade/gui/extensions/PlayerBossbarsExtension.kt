package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.BossEvent
import java.util.*

internal class PlayerBossbarsExtension(
    owner: ServerGamePacketListenerImpl
): PlayerExtension(owner) {
    private val bossbars = HashMap<CustomBossBar, PlayerBossEvent>()

    internal fun tick() {
        for ((bar, data) in this.bossbars) {
            if (data.ticks++ % bar.interval != 0) {
                continue
            }

            this.updateTitle(bar)
            this.updateProgress(bar)
            this.updateStyle(bar)
            this.updateProperties(bar)
        }
    }

    internal fun add(bar: CustomBossBar) {
        if (this.bossbars.containsKey(bar)) {
            return
        }

        val data = PlayerBossEvent(
            bar.uuid,
            bar.getTitle(this.player),
            bar.getColour(this.player),
            bar.getOverlay(this.player)
        )
        data.progress = bar.getProgress(this.player)
        data.setDarkenScreen(bar.isDark(this.player))
        data.setPlayBossMusic(bar.hasMusic(this.player))
        data.setCreateWorldFog(bar.hasFog(this.player))
        this.bossbars[bar] = data
        this.player.connection.send(ClientboundBossEventPacket.createAddPacket(data))
    }

    internal fun remove(bar: CustomBossBar) {
        this.bossbars.remove(bar)
        this.player.connection.send(ClientboundBossEventPacket.createRemovePacket(bar.uuid))
    }

    internal fun updateTitle(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val new = bar.getTitle(this.player)
        if (new != data.name) {
            data.name = new
            this.player.connection.send(ClientboundBossEventPacket.createUpdateNamePacket(data))
        }
    }

    internal fun updateProgress(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val new = bar.getProgress(this.player)
        if (new != data.progress) {
            data.progress = new
            this.player.connection.send(ClientboundBossEventPacket.createUpdateProgressPacket(data))
        }
    }

    internal fun updateStyle(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val newColour = bar.getColour(this.player)
        val newOverlay = bar.getOverlay(this.player)
        if (newColour != data.color || newOverlay != data.overlay) {
            data.color = newColour
            data.overlay = newOverlay
            this.player.connection.send(ClientboundBossEventPacket.createUpdateStylePacket(data))
        }
    }

    internal fun updateProperties(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val newDark = bar.isDark(this.player)
        val newMusic = bar.hasMusic(this.player)
        val newFog = bar.hasFog(this.player)
        if (newDark != data.shouldDarkenScreen() || newMusic != data.shouldPlayBossMusic() || newFog != data.shouldCreateWorldFog()) {
            data.setDarkenScreen(newDark)
            data.setPlayBossMusic(newMusic)
            data.setCreateWorldFog(newFog)
            this.player.connection.send(ClientboundBossEventPacket.createUpdatePropertiesPacket(data))
        }
    }

    internal fun disconnect() {
        for (bar in LinkedList(this.bossbars.keys)) {
            bar.removePlayer(this.player)
        }
    }

    internal fun getEvent(bar: CustomBossBar): BossEvent? {
        return this.bossbars[bar]
    }

    private class PlayerBossEvent(
        uuid: UUID,
        title: Component,
        colour: BossBarColor,
        overlay: BossBarOverlay,
    ): BossEvent(uuid, title, colour, overlay) {
        var ticks = 0
    }
}