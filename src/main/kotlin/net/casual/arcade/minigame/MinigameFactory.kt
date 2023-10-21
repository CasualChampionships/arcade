package net.casual.arcade.minigame

import net.minecraft.server.MinecraftServer

public interface MinigameFactory {
    public fun create(server: MinecraftServer): Minigame<*>
}