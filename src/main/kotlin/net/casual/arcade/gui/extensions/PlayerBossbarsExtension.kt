package net.casual.arcade.gui.extensions

import net.casual.arcade.extensions.Extension
import net.casual.arcade.gui.bossbar.CustomBossBar
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import java.util.*

internal class PlayerBossbarsExtension(
    private val owner: ServerPlayer
): Extension {
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
            bar.getTitle(this.owner),
            bar.getColour(this.owner),
            bar.getOverlay(this.owner)
        )
        data.progress = bar.getProgress(this.owner)
        data.setDarkenScreen(bar.isDark(this.owner))
        data.setPlayBossMusic(bar.hasMusic(this.owner))
        data.setCreateWorldFog(bar.hasFog(this.owner))
        this.bossbars[bar] = data
        this.owner.connection.send(ClientboundBossEventPacket.createAddPacket(data))
    }

    internal fun remove(bar: CustomBossBar) {
        this.bossbars.remove(bar)
        this.owner.connection.send(ClientboundBossEventPacket.createRemovePacket(bar.uuid))
    }

    internal fun updateTitle(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val new = bar.getTitle(this.owner)
        if (new != data.name) {
            data.name = new
            this.owner.connection.send(ClientboundBossEventPacket.createUpdateNamePacket(data))
        }
    }

    internal fun updateProgress(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val new = bar.getProgress(this.owner)
        if (new != data.progress) {
            data.progress = new
            this.owner.connection.send(ClientboundBossEventPacket.createUpdateProgressPacket(data))
        }
    }

    internal fun updateStyle(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val newColour = bar.getColour(this.owner)
        val newOverlay = bar.getOverlay(this.owner)
        if (newColour != data.color || newOverlay != data.overlay) {
            data.color = newColour
            data.overlay = newOverlay
            this.owner.connection.send(ClientboundBossEventPacket.createUpdateStylePacket(data))
        }
    }

    internal fun updateProperties(bar: CustomBossBar) {
        val data = this.bossbars[bar] ?: return
        val newDark = bar.isDark(this.owner)
        val newMusic = bar.hasMusic(this.owner)
        val newFog = bar.hasFog(this.owner)
        if (newDark != data.shouldDarkenScreen() || newMusic != data.shouldPlayBossMusic() || newFog != data.shouldCreateWorldFog()) {
            data.setDarkenScreen(newDark)
            data.setPlayBossMusic(newMusic)
            data.setCreateWorldFog(newFog)
            this.owner.connection.send(ClientboundBossEventPacket.createUpdatePropertiesPacket(data))
        }
    }

    internal fun disconnect() {
        for (bar in LinkedList(this.bossbars.keys)) {
            bar.removePlayer(this.owner)
        }
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