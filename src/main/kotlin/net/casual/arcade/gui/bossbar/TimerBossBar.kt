package net.casual.arcade.gui.bossbar

import net.casual.arcade.gui.TickableUI
import net.casual.arcade.gui.suppliers.TimedComponentSupplier
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

class TimerBossBar(
    duration: MinecraftTimeDuration,
    private val colour: BossBarColor,
    private val overlay: BossBarOverlay,
    private val title: TimedComponentSupplier,
): CustomBossBar(), TickableUI {
    private var ticks = duration.toTicks()
    private var tick = 0

    override fun tick() {
        if (this.tick < this.ticks) {
            this.tick++
        }
    }

    override fun getTitle(player: ServerPlayer): Component {
        return this.title.getComponent(player, Ticks.duration(this.ticks - this.tick))
    }

    override fun getProgress(player: ServerPlayer): Float {
        return this.tick / this.ticks.toFloat()
    }

    override fun getColour(player: ServerPlayer): BossBarColor {
        return this.colour
    }

    override fun getOverlay(player: ServerPlayer): BossBarOverlay {
        return this.overlay
    }
}