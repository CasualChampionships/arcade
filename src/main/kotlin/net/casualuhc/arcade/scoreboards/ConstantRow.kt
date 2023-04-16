package net.casualuhc.arcade.scoreboards

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

class ConstantRow(
    private val component: Component
): SidebarRow {
    override fun getComponent(player: ServerPlayer): Component {
        return this.component
    }
}