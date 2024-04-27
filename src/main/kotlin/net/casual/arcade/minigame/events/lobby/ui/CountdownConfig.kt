package net.casual.arcade.minigame.events.lobby.ui

import net.casual.arcade.gui.countdown.Countdown
import net.casual.arcade.gui.countdown.TitledCountdown.Companion.DEFAULT_TITLE
import net.casual.arcade.utils.ComponentUtils.toFormattedString

public interface CountdownConfig {
    public fun create(): Countdown

    public companion object {
        public val DEFAULT: TitledCountdownConfig = TitledCountdownConfig(DEFAULT_TITLE.toFormattedString())
    }
}