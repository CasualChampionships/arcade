package net.casual.arcade.commands.hidden

import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerPlayer

public class HiddenCommandContext(
    public val player: ServerPlayer
) {
    private var removed = false

    public val server: MinecraftServer
        get() = this.player.server

    public fun remove() {
        this.removed = true
    }

    internal fun removed(): Boolean {
        return this.removed
    }
}
