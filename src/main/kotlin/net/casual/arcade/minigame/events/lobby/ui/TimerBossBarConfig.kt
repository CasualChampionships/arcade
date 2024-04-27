package net.casual.arcade.minigame.events.lobby.ui

import net.casual.arcade.gui.bossbar.TimerBossBar

public interface TimerBossBarConfig {
    public fun create(): TimerBossBar

    public companion object {
        public val DEFAULT: SimpleTimerBossbarConfig = SimpleTimerBossbarConfig()
    }
}