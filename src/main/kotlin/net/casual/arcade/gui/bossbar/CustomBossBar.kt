package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.PlayerUI
import net.casual.arcade.utils.BossbarUtils.bossbars
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.util.Mth
import net.minecraft.world.BossEvent
import java.util.*

abstract class CustomBossBar: PlayerUI() {
    internal val uuid: UUID = Mth.createInsecureUUID()

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

    override fun onAddPlayer(player: ServerPlayer) {
        player.bossbars.add(this)
    }

    override fun onRemovePlayer(player: ServerPlayer) {
        player.bossbars.remove(this)
    }
}