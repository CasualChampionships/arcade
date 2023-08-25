package net.casual.arcade.minigame

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

interface MinigameResources {
    fun getInfo(): MinecraftServer.ServerResourcePackInfo? {
        return null
    }

    fun getInfo(player: ServerPlayer): MinecraftServer.ServerResourcePackInfo? {
        return this.getInfo()
    }
}