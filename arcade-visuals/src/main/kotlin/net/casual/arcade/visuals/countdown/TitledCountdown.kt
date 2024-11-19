package net.casual.arcade.visuals.countdown

import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.yellow
import net.casual.arcade.utils.PlayerUtils.clearTitle
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.PlayerUtils.setTitleAnimation
import net.casual.arcade.utils.TimeUtils.Ticks
import net.casual.arcade.utils.impl.Sound
import net.casual.arcade.utils.time.MinecraftTimeDuration
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import org.jetbrains.annotations.ApiStatus.OverrideOnly

public interface TitledCountdown: Countdown {
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

    override fun beforeCountdown(players: Collection<ServerPlayer>, interval: MinecraftTimeDuration) {
        for (player in players) {
            player.setTitleAnimation(0.Ticks, interval * 2, 0.Ticks)
        }
    }

    override fun sendCountdown(players: Collection<ServerPlayer>, current: Int, remaining: MinecraftTimeDuration) {
        val title = this.getCountdownTitle(current)
        val subtitle = this.getCountdownSubtitle(current)
        val sound = this.getCountdownSound(current)
        for (player in players) {
            player.sendTitle(title, subtitle)
            if (sound != null) {
                player.sendSound(sound)
            }
        }
    }

    override fun afterCountdown(players: Collection<ServerPlayer>) {
        for (player in players) {
            player.clearTitle()
        }
    }

    public companion object {
        @JvmField
        public val DEFAULT_TITLE: Component = Component.literal("Starting In:").bold()

        public fun titled(title: Component = DEFAULT_TITLE): Countdown {
            return object: TitledCountdown {
                override fun getCountdownTitle(current: Int): Component {
                    return title
                }
            }
        }
    }
}