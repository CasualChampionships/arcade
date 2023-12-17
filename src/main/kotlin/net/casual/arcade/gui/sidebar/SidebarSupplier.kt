package net.casual.arcade.gui.sidebar

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public fun interface SidebarSupplier {
    public fun getComponent(player: ServerPlayer): SidebarComponent

    public companion object {
        public fun empty(): SidebarSupplier {
            return withNoScore(Component.empty())
        }

        public fun of(component: SidebarComponent): SidebarSupplier {
            return Constant(component)
        }

        public fun withNoScore(component: Component): SidebarSupplier {
            return of(SidebarComponent.withNoScore(component))
        }

        public fun withCustomScore(component: Component, score: Component): SidebarSupplier {
            return of(SidebarComponent.withCustomScore(component, score))
        }
    }

    private class Constant(private val component: SidebarComponent): SidebarSupplier {
        override fun getComponent(player: ServerPlayer): SidebarComponent {
            return this.component
        }
    }
}