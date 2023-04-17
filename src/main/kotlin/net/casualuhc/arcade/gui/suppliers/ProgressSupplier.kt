package net.casualuhc.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer

fun interface ProgressSupplier {
    fun getProgress(player: ServerPlayer): Float

    companion object {
        fun of(progress: Float): ProgressSupplier {
            return Constant(progress)
        }
    }

    private class Constant(private val progress: Float): ProgressSupplier {
        override fun getProgress(player: ServerPlayer): Float {
            return this.progress
        }
    }
}