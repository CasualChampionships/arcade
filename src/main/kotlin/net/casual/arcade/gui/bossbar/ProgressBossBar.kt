package net.casual.arcade.gui.bossbar

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

class ProgressBossBar(
    duration: MinecraftTimeDuration,
    private val colour: BossBarColor,
    private val overlay: BossBarOverlay,
    private val title: TitleGenerator,
): CustomBossBar() {
    private var ticks = duration.toTicks()
    private var tick = 0

    fun tick() {
        if (this.tick < this.ticks) {
            this.tick++
        }
    }

    fun getProgress(): Float {
        return this.tick / this.ticks.toFloat()
    }

    override fun getTitle(player: ServerPlayer): Component {
        return this.title.generate(player, Ticks.duration(this.ticks - this.tick))
    }

    override fun getProgress(player: ServerPlayer): Float {
        return this.getProgress()
    }

    override fun getColour(player: ServerPlayer): BossBarColor {
        return this.colour
    }

    override fun getOverlay(player: ServerPlayer): BossBarOverlay {
        return this.overlay
    }

    interface TitleGenerator {
        fun generate(player: ServerPlayer, duration: MinecraftTimeDuration): Component
    }
}