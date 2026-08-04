/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.bossbar

import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.casual.arcade.visuals.core.TickableVisualElement
import net.minecraft.network.chat.Component
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent

public abstract class TimerBossbar: CustomBossbar(), TickableVisualElement {
    private var ticks = -1
    private var tick = 0

    public var complete: Boolean = false
        private set

    public val hasDuration: Boolean
        get() = this.ticks != -1

    override fun tick(server: MinecraftServer) {
        if (this.ticks == -1) {
            return
        }
        if (this.tick < this.ticks) {
            this.tick++
            return
        }
        this.complete = true
    }

    public fun setDuration(duration: MinecraftTimeDuration) {
        this.complete = false
        this.tick = 0
        this.ticks = duration.ticks
    }

    public fun setRemainingDuration(duration: MinecraftTimeDuration) {
        if (!this.hasDuration) {
            return
        }
        this.tick = this.ticks - duration.ticks
    }

    public fun removeDuration() {
        this.complete = true
        this.ticks = -1
    }

    public fun getProgress(): Float {
        return if (!this.hasDuration) 0.0F else this.tick / this.ticks.toFloat()
    }

    public fun getRemainingDuration(): MinecraftTimeDuration {
        return if (!this.hasDuration) 0.Ticks else (this.ticks - this.tick).Ticks
    }

    /**
     * This gets the progress of the [CustomBossbar] which will be
     * displayed to the given [player].
     *
     * @param player The player being displayed the progress.
     * @return The progress to display the bossbar as having.
     */
    override fun getProgress(player: ServerPlayer): Float {
        return this.getProgress()
    }

    public companion object {
        public val DEFAULT: TimerBossbar = create()

        public fun create(
            color: BossEvent.BossBarColor = BossEvent.BossBarColor.YELLOW,
            overlay: BossEvent.BossBarOverlay = BossEvent.BossBarOverlay.PROGRESS,
            title: (TimerBossbar) -> Component = { Component.literal(it.getRemainingDuration().formatHHMMSS()) }
        ): TimerBossbar {
            return object: TimerBossbar() {
                override fun getTitle(player: ServerPlayer): Component {
                    return title.invoke(this)
                }

                override fun getColor(player: ServerPlayer): BossEvent.BossBarColor {
                    return color
                }

                override fun getOverlay(player: ServerPlayer): BossEvent.BossBarOverlay {
                    return overlay
                }
            }
        }
    }
}