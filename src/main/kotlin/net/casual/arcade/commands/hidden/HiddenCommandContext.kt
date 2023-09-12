package net.casual.arcade.commands.hidden

import net.minecraft.server.level.ServerPlayer

public class HiddenCommandContext(
    public val player: ServerPlayer
) {
    internal var remove = false
        private set

    public fun remove() {
        this.remove = true
    }
}