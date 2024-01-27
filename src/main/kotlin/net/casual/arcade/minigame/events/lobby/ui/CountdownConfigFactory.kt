package net.casual.arcade.minigame.events.lobby.ui

import com.google.gson.JsonObject

public interface CountdownConfigFactory {
    public val id: String

    public fun create(data: JsonObject): CountdownConfig
}