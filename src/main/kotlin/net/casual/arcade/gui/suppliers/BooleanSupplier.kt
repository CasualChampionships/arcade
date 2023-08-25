package net.casual.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer

fun interface BooleanSupplier {
    fun get(player: ServerPlayer): Boolean

    companion object {
        private val TRUE = BooleanSupplier { true }
        private val FALSE = BooleanSupplier { false }

        fun alwaysTrue(): BooleanSupplier {
            return TRUE
        }

        fun alwaysFalse(): BooleanSupplier {
            return FALSE
        }
    }
}