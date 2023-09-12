package net.casual.arcade.gui.suppliers

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public fun interface ComponentSupplier {
    public fun getComponent(player: ServerPlayer): Component

    public companion object {
        public fun of(component: Component): ComponentSupplier {
            return Constant(component)
        }
    }

    private class Constant(private val component: Component): ComponentSupplier {
        override fun getComponent(player: ServerPlayer): Component {
            return this.component
        }
    }
}