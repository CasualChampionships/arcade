package net.casual.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer

public fun interface ProgressSupplier {
    public fun getProgress(player: ServerPlayer): Float

    public companion object {
        public fun of(progress: Float): ProgressSupplier {
            return Constant(progress)
        }
    }

    private class Constant(private val progress: Float): ProgressSupplier {
        override fun getProgress(player: ServerPlayer): Float {
            return this.progress
        }
    }
}