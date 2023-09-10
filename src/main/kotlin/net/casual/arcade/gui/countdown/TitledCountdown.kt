package net.casual.arcade.gui.countdown

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.yellow
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.impl.Sound
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import org.jetbrains.annotations.ApiStatus.OverrideOnly

interface TitledCountdown: Countdown {
    @OverrideOnly
    fun getCountdownTitle(current: Int): Component {
        return Component.literal("Starting In:").bold()
    }

    @OverrideOnly
    fun getCountdownSubtitle(current: Int): Component {
        val subtitle = Component.literal("▶ $current ◀")
        when (current) {
            3 -> subtitle.red()
            2 -> subtitle.yellow()
            1 -> subtitle.lime()
        }
        return subtitle
    }

    @OverrideOnly
    fun getCountdownSound(current: Int): Sound? {
        return Sound(
            sound = SoundEvents.NOTE_BLOCK_PLING.value(),
            pitch = 3.0F
        )
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

    companion object {
        val DEFAULT = object: TitledCountdown { }

        fun titled(title: Component): TitledCountdown {
            return object: TitledCountdown {
                override fun getCountdownTitle(remainingSeconds: Int): Component {
                    return title
                }
            }
        }
    }
}