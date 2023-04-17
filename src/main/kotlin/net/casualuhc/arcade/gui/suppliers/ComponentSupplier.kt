package net.casualuhc.arcade.gui.suppliers

import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

fun interface ComponentSupplier {
    fun getComponent(player: ServerPlayer): Component

    companion object {
        fun of(component: Component): ComponentSupplier {
            return Constant(component)
        }
    }

    private class Constant(private val component: Component): ComponentSupplier {
        override fun getComponent(player: ServerPlayer): Component {
            return this.component
        }
    }
}