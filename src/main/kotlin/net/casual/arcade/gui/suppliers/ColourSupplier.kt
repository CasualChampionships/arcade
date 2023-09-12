package net.casual.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarColor

public fun interface ColourSupplier {
    public fun getColour(player: ServerPlayer): BossBarColor

    public companion object {
        public fun of(colour: BossBarColor): ColourSupplier {
            return Constant(colour)
        }
    }

    private class Constant(private val component: BossBarColor): ColourSupplier {
        override fun getColour(player: ServerPlayer): BossBarColor {
            return this.component
        }
    }
}