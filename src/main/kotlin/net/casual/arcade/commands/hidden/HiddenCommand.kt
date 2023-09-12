package net.casual.arcade.commands.hidden

import net.minecraft.server.level.ServerPlayer

public fun interface HiddenCommand {
    public fun run(context: HiddenCommandContext)

    public fun canRun(player: ServerPlayer): Boolean {
        return true
    }
}