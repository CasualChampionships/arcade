package net.casual.arcade.minigame.events.lobby.ui

import com.google.gson.JsonObject

public interface TimerBossBarConfigFactory {
    public val id: String

    public fun create(data: JsonObject): TimerBossBarConfig
}