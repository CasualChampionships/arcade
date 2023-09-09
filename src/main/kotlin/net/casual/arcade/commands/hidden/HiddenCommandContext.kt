package net.casual.arcade.commands.hidden

import net.minecraft.server.level.ServerPlayer

class HiddenCommandContext(
    val player: ServerPlayer
) {
    internal var remove = false
        private set

    fun remove() {
        this.remove = true
    }
}