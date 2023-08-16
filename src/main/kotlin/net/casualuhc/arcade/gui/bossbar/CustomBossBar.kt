package net.casualuhc.arcade.gui.bossbar

import net.casualuhc.arcade.utils.BossbarUtils.bossbars
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.util.Mth
import net.minecraft.world.BossEvent
import java.util.*

abstract class CustomBossBar {
    private val connections = HashSet<ServerGamePacketListenerImpl>()
    internal val uuid: UUID = Mth.createInsecureUUID()

    internal var interval = 1
        private set

    abstract fun getTitle(player: ServerPlayer): Component

    abstract fun getProgress(player: ServerPlayer): Float

    abstract fun getColour(player: ServerPlayer): BossEvent.BossBarColor

    abstract fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay

    open fun isDark(player: ServerPlayer): Boolean {
        return false
    }

    open fun hasMusic(player: ServerPlayer): Boolean {
        return false
    }

    open fun hasFog(player: ServerPlayer): Boolean {
        return false
    }

    fun setUpdateInterval(interval: Int) {
        this.interval = interval.coerceAtLeast(1)
    }

    fun addPlayer(player: ServerPlayer) {
        if (this.connections.add(player.connection)) {
            player.bossbars.add(this)
        }
    }

    fun removePlayer(player: ServerPlayer) {
        if (this.connections.remove(player.connection)) {
            player.bossbars.remove(this)
        }
    }

    fun clearPlayers() {
        for (player in this.getPlayers()) {
            this.removePlayer(player)
        }
    }

    fun getPlayers(): List<ServerPlayer> {
        return this.connections.map { it.player }
    }
}