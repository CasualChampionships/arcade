package net.casual.arcade.commands.hidden

import net.minecraft.server.level.ServerPlayer

fun interface HiddenCommand {
    fun run(context: HiddenCommandContext)

    fun canRun(player: ServerPlayer): Boolean {
        return true
    }
}