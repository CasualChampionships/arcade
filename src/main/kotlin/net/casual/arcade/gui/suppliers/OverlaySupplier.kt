package net.casual.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.BossEvent.BossBarOverlay

public fun interface OverlaySupplier {
    public fun getOverlay(player: ServerPlayer): BossBarOverlay

    public companion object {
        public fun of(overlay: BossBarOverlay): OverlaySupplier {
            return Constant(overlay)
        }
    }

    private class Constant(private val overlay: BossBarOverlay): OverlaySupplier {
        override fun getOverlay(player: ServerPlayer): BossBarOverlay {
            return this.overlay
        }
    }
}