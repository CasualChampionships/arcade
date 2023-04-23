package net.casualuhc.arcade.gui

import net.casualuhc.arcade.extensions.Extension
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import java.util.*

class PlayerBossbarsExtension(
    private val owner: ServerPlayer
): Extension {
    private val bossbars = HashMap<ArcadeBossbar, PlayerBossEvent>()

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

    internal fun add(bar: ArcadeBossbar) {
        if (this.bossbars.containsKey(bar)) {
            return
        }

        val data = PlayerBossEvent(
            bar.uuid,
            bar.title.getComponent(this.owner),
            bar.colour.getColour(this.owner),
            bar.overlay.getOverlay(this.owner)
        )
        data.progress = bar.progress.getProgress(this.owner)
        data.setDarkenScreen(bar.dark.get(this.owner))
        data.setPlayBossMusic(bar.music.get(this.owner))
        data.setCreateWorldFog(bar.fog.get(this.owner))
        this.bossbars[bar] = data
        this.owner.connection.send(ClientboundBossEventPacket.createAddPacket(data))
    }

    internal fun remove(bar: ArcadeBossbar) {
        this.bossbars.remove(bar)
        this.owner.connection.send(ClientboundBossEventPacket.createRemovePacket(bar.uuid))
    }

    internal fun updateTitle(bar: ArcadeBossbar) {
        val data = this.bossbars[bar] ?: return
        val new = bar.title.getComponent(this.owner)
        if (new != data.name) {
            data.name = new
            this.owner.connection.send(ClientboundBossEventPacket.createUpdateNamePacket(data))
        }
    }

    internal fun updateProgress(bar: ArcadeBossbar) {
        val data = this.bossbars[bar] ?: return
        val new = bar.progress.getProgress(this.owner)
        if (new != data.progress) {
            data.progress = new
            this.owner.connection.send(ClientboundBossEventPacket.createUpdateProgressPacket(data))
        }
    }

    internal fun updateStyle(bar: ArcadeBossbar) {
        val data = this.bossbars[bar] ?: return
        val newColour = bar.colour.getColour(this.owner)
        val newOverlay = bar.overlay.getOverlay(this.owner)
        if (newColour != data.color || newOverlay != data.overlay) {
            data.color = newColour
            data.overlay = newOverlay
            this.owner.connection.send(ClientboundBossEventPacket.createUpdateStylePacket(data))
        }
    }

    internal fun updateProperties(bar: ArcadeBossbar) {
        val data = this.bossbars[bar] ?: return
        val newDark = bar.dark.get(this.owner)
        val newMusic = bar.music.get(this.owner)
        val newFog = bar.fog.get(this.owner)
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