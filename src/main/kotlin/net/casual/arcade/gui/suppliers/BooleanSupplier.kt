package net.casual.arcade.gui.suppliers

import net.minecraft.server.level.ServerPlayer

public fun interface BooleanSupplier {
    public fun get(player: ServerPlayer): Boolean

    public companion object {
        private val TRUE = BooleanSupplier { true }
        private val FALSE = BooleanSupplier { false }

        public fun alwaysTrue(): BooleanSupplier {
            return TRUE
        }

        public fun alwaysFalse(): BooleanSupplier {
            return FALSE
        }
    }
}