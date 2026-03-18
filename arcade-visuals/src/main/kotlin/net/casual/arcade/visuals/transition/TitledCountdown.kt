/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.visuals.transition

import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.component.bold
import net.casual.arcade.utils.component.lime
import net.casual.arcade.utils.component.red
import net.casual.arcade.utils.component.yellow
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.player.sendSound
import net.casual.arcade.utils.player.sendTitle
import net.casual.arcade.utils.player.setTitleAnimation
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface TitledCountdown: Transition {
    @OverrideOnly
    public fun getCountdownTitle(current: Int): Component {
        return DEFAULT_TITLE
    }

    @OverrideOnly
    public fun getCountdownSubtitle(current: Int): Component {
        val subtitle = Component.literal("▶ $current ◀")
        when (current) {
            3 -> subtitle.red()
            2 -> subtitle.yellow()
            1 -> subtitle.lime()
        }
        return subtitle
    }

    @OverrideOnly
    public fun getCountdownSound(current: Int): Sound? {
        return Sound(
            event = SoundEvents.NOTE_BLOCK_PLING.value(),
            pitch = 3.0F
        )
    }

    override fun beforeTransition(
        players: Collection<ServerPlayer>,
        interval: MinecraftTimeDuration,
        updates: Int
    ) {
        for (player in players) {
            player.setTitleAnimation(0.Ticks, interval * 2, 0.Ticks)
        }
    }

    override fun updateTransition(
        players: Collection<ServerPlayer>,
        current: Int,
        total: Int,
        remaining: MinecraftTimeDuration
    ) {
        val index = total - current
        val title = this.getCountdownTitle(index)
        val subtitle = this.getCountdownSubtitle(index)
        val sound = this.getCountdownSound(index)
        for (player in players) {
            player.sendTitle(title, subtitle)
            if (sound != null) {
                player.sendSound(sound)
            }
        }
    }

    override fun afterTransition(players: Collection<ServerPlayer>) {

    }

    public companion object {
        @JvmField
        public val DEFAULT_TITLE: Component = Component.literal("Starting In:").bold()

        public fun titled(title: Component = DEFAULT_TITLE): TitledCountdown {
            return object: TitledCountdown {
                override fun getCountdownTitle(current: Int): Component {
                    return title
                }
            }
        }
    }
}