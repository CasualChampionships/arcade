package net.casual.arcade.utils.minigame

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.utils.ComponentUtils.bold
import net.casual.arcade.utils.ComponentUtils.lime
import net.casual.arcade.utils.ComponentUtils.red
import net.casual.arcade.utils.ComponentUtils.yellow
import net.casual.arcade.utils.MathUtils.wholeOrNull
import net.casual.arcade.utils.PlayerUtils.sendSound
import net.casual.arcade.utils.PlayerUtils.sendTitle
import net.casual.arcade.utils.impl.Sound
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import org.jetbrains.annotations.ApiStatus.OverrideOnly

interface TitledMinigameCountdown: MinigameCountdown {
    @OverrideOnly
    fun getCountdownTitle(remainingSeconds: Int): Component {
        return Component.literal("Starting In:").bold()
    }

    @OverrideOnly
    fun getCountdownSubtitle(remainingSeconds: Int): Component {
        val subtitle = Component.literal("▶ $remainingSeconds ◀")
        when (remainingSeconds) {
            3 -> subtitle.red()
            2 -> subtitle.yellow()
            1 -> subtitle.lime()
        }
        return subtitle
    }

    @OverrideOnly
    fun getCountdownSound(remainingSeconds: Int): Sound? {
        return Sound(
            sound = SoundEvents.NOTE_BLOCK_PLING.value(),
            pitch = 3.0F
        )
    }

    override fun sendCountdown(players: Collection<ServerPlayer>, remaining: MinecraftTimeDuration) {
        val seconds = remaining.toSeconds().wholeOrNull() ?: return
        val title = this.getCountdownTitle(seconds)
        val subtitle = this.getCountdownSubtitle(seconds)
        val sound = this.getCountdownSound(seconds)
        for (player in players) {
            player.sendTitle(title, subtitle)
            if (sound != null) {
                player.sendSound(sound)
            }
        }
    }

    companion object {
        val DEFAULT = object: TitledMinigameCountdown { }

        fun titled(title: Component): TitledMinigameCountdown {
            return object: TitledMinigameCountdown {
                override fun getCountdownTitle(remainingSeconds: Int): Component {
                    return title
                }
            }
        }
    }
}