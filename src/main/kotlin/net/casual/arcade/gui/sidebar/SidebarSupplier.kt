package net.casual.arcade.gui.sidebar

import net.minecraft.server.level.ServerPlayer

public fun interface SidebarSupplier {
    public fun getComponent(player: ServerPlayer): SidebarComponent
}