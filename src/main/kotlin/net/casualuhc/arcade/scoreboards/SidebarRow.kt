package net.casualuhc.arcade.scoreboards

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

fun interface SidebarRow {
    fun getComponent(player: ServerPlayer): Component
}