package net.casualuhc.arcade.scoreboards

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

interface SidebarRow {
    fun getComponent(player: ServerPlayer): Component
}