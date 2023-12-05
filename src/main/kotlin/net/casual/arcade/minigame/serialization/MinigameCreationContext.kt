package net.casual.arcade.minigame.serialization

import com.google.gson.JsonObject
import net.minecraft.server.MinecraftServer

public class MinigameCreationContext(
    public val server: MinecraftServer,
    private val data: JsonObject? = null
) {
    public fun hasCustomData(): Boolean {
        return this.data != null
    }

    public fun getCustomData(): JsonObject {
        return this.data ?: throw IllegalStateException("Tried getting save data when there was none!")
    }
}