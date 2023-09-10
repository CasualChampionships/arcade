package net.casual.arcade.gui.suppliers

import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

interface TimedComponentSupplier {
    fun getComponent(player: ServerPlayer, duration: MinecraftTimeDuration): Component
}