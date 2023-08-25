package net.casual.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor

fun interface ColourSupplier {
    fun getColour(player: ServerPlayer): BossBarColor

    companion object {
        fun of(colour: BossBarColor): ColourSupplier {
            return Constant(colour)
        }
    }

    private class Constant(private val component: BossBarColor): ColourSupplier {
        override fun getColour(player: ServerPlayer): BossBarColor {
            return this.component
        }
    }
}