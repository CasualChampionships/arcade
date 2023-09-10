package net.casual.arcade.gui.bossbar

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

data class StaticBossBar(
    val title: Component,
    val progress: Float = 1.0F,
    val colour: BossBarColor = BossBarColor.WHITE,
    val overlay: BossBarOverlay = BossBarOverlay.PROGRESS,
    val dark: Boolean = false,
    val music: Boolean = false,
    val fog: Boolean = false
):  CustomBossBar() {
    override fun getTitle(player: ServerPlayer): Component {
        return this.title
    }

    override fun getProgress(player: ServerPlayer): Float {
        return this.progress
    }

    override fun getColour(player: ServerPlayer): BossBarColor {
        return this.colour
    }

    override fun getOverlay(player: ServerPlayer): BossBarOverlay {
        return this.overlay
    }

    override fun isDark(player: ServerPlayer): Boolean {
        return this.dark
    }

    override fun hasMusic(player: ServerPlayer): Boolean {
        return this.music
    }

    override fun hasFog(player: ServerPlayer): Boolean {
        return this.fog
    }
}