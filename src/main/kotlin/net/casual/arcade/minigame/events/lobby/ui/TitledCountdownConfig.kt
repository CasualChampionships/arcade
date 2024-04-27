package net.casual.arcade.minigame.events.lobby.ui

import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown
import net.casual.arcade.utils.ComponentUtils.literal

public class TitledCountdownConfig(
    private val title: String
): CountdownConfig {
    override fun create(): Countdown {
        return TitledCountdown.titled(this.title.literal())
    }
}