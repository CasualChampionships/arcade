package net.casual.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarOverlay

fun interface OverlaySupplier {
    fun getOverlay(player: ServerPlayer): BossBarOverlay

    companion object {
        fun of(overlay: BossBarOverlay): OverlaySupplier {
            return Constant(overlay)
        }
    }

    private class Constant(private val overlay: BossBarOverlay): OverlaySupplier {
        override fun getOverlay(player: ServerPlayer): BossBarOverlay {
            return this.overlay
        }
    }
}