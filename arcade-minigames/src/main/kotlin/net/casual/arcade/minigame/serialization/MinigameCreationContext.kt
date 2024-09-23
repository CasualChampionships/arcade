package net.casual.arcade.minigame.serialization

import com.google.gson.JsonObject
import net.minecraft.server.MinecraftServer

public class MinigameCreationContext(
    public val server: MinecraftServer,
    public val parameters: JsonObject = JsonObject()
)