package net.casual.arcade.gui.sidebar

import net.minecraft.server.level.ServerPlayer

public fun interface SidebarSupplier {
    public fun getComponent(player: ServerPlayer): SidebarComponent

    public companion object {
        public fun of(component: SidebarComponent): SidebarSupplier {
            return Constant(component)
        }
    }

    private class Constant(private val component: SidebarComponent): SidebarSupplier {
        override fun getComponent(player: ServerPlayer): SidebarComponent {
            return this.component
        }
    }
}