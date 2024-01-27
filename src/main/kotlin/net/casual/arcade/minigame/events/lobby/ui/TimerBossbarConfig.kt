package net.casual.arcade.minigame.events.lobby.ui

import com.google.gson.JsonObject
import net.casual.arcade.gui.bossbar.TimerBossBar
import net.minecraft.world.BossEvent.BossBarColor.YELLOW
import net.minecraft.world.BossEvent.BossBarOverlay.PROGRESS

public interface TimerBossbarConfig {
    public val id: String

    public fun create(): TimerBossBar

    public fun write(): JsonObject

    public companion object {
        public val DEFAULT: SimpleTimerBossbarConfig = SimpleTimerBossbarConfig("Starting In: %s", PROGRESS, YELLOW)
    }
}