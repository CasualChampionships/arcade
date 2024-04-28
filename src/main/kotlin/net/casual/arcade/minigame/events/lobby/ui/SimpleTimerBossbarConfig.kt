package net.casual.arcade.minigame.events.lobby.ui

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.casual.arcade.utils.ComponentUtils.literal
import net.casual.arcade.utils.TimeUtils.formatHHMMSS
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor
import net.minecraft.world.BossEvent.BossBarOverlay

@Serializable
@SerialName("simple")
public class SimpleTimerBossbarConfig(
    private val title: String = "Starting In: %s",
    private val overlay: BossBarOverlay = BossBarOverlay.PROGRESS,
    private val colour: BossBarColor = BossBarColor.YELLOW,
): TimerBossBarConfig {
    override fun create(): TimerBossBar {
        return object: TimerBossBar() {
            override fun getTitle(player: ServerPlayer): Component {
                return title.format(this.getRemainingDuration().formatHHMMSS()).literal()
            }

            override fun getColour(player: ServerPlayer): BossBarColor {
                return colour
            }

            override fun getOverlay(player: ServerPlayer): BossBarOverlay {
                return overlay
            }
        }
    }
}