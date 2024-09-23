package net.casual.arcade.visuals.extensions

import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.player.PlayerLeaveEvent
import net.casual.arcade.events.player.PlayerTickEvent
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.PlayerExtensionEvent
import net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.getExtension
import net.casual.arcade.visuals.bossbar.CustomBossbar
import net.minecraft.network.chat.Component
import net.minecraft.network.protocol.game.ClientboundBossEventPacket
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent
import java.util.*

internal class PlayerBossbarsExtension(
    owner: ServerPlayer
): PlayerExtension(owner) {
    private val bars = HashMap<CustomBossbar, PlayerBossEvent>()

    internal fun tick() {
        for ((bar, data) in this.bars) {
            if (data.ticks++ % bar.interval != 0) {
                continue
            }

            this.updateTitle(bar)
            this.updateProgress(bar)
            this.updateStyle(bar)
            this.updateProperties(bar)
        }
    }

    internal fun add(bar: CustomBossbar) {
        if (this.bars.containsKey(bar)) {
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
        this.bars[bar] = data
        this.player.connection.send(ClientboundBossEventPacket.createAddPacket(data))
    }

    internal fun remove(bar: CustomBossbar) {
        this.bars.remove(bar)
        this.player.connection.send(ClientboundBossEventPacket.createRemovePacket(bar.uuid))
    }

    internal fun updateTitle(bar: CustomBossbar) {
        val data = this.bars[bar] ?: return
        val new = bar.getTitle(this.player)
        if (new != data.name) {
            data.name = new
            this.player.connection.send(ClientboundBossEventPacket.createUpdateNamePacket(data))
        }
    }

    internal fun updateProgress(bar: CustomBossbar) {
        val data = this.bars[bar] ?: return
        val new = bar.getProgress(this.player)
        if (new != data.progress) {
            data.progress = new
            this.player.connection.send(ClientboundBossEventPacket.createUpdateProgressPacket(data))
        }
    }

    internal fun updateStyle(bar: CustomBossbar) {
        val data = this.bars[bar] ?: return
        val newColour = bar.getColour(this.player)
        val newOverlay = bar.getOverlay(this.player)
        if (newColour != data.color || newOverlay != data.overlay) {
            data.color = newColour
            data.overlay = newOverlay
            this.player.connection.send(ClientboundBossEventPacket.createUpdateStylePacket(data))
        }
    }

    internal fun updateProperties(bar: CustomBossbar) {
        val data = this.bars[bar] ?: return
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
        for (bar in LinkedList(this.bars.keys)) {
            bar.removePlayer(this.player)
        }
    }

    internal fun getEvent(bar: CustomBossbar): BossEvent? {
        return this.bars[bar]
    }

    private class PlayerBossEvent(
        uuid: UUID,
        title: Component,
        colour: BossBarColor,
        overlay: BossBarOverlay,
    ): BossEvent(uuid, title, colour, overlay) {
        var ticks = 0
    }

    companion object {
        internal val ServerPlayer.bossbars
            get() = this.getExtension<PlayerBossbarsExtension>()

        internal fun registerEvents() {
            GlobalEventHandler.register<PlayerExtensionEvent> { event ->
                event.addExtension(::PlayerBossbarsExtension)
            }
            GlobalEventHandler.register<PlayerLeaveEvent> { (player) ->
                player.bossbars.disconnect()
            }
            GlobalEventHandler.register<PlayerTickEvent> { (player) ->
                player.bossbars.tick()
            }
        }
    }
}