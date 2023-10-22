package net.casual.arcade.commands.hidden

import net.casual.arcade.utils.ComponentUtils.crimson
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer

public class HiddenCommandContext(
    public val player: ServerPlayer
) {
    internal var removedMessage: ((ServerPlayer) -> Component)? = null
        private set

    public fun removeCommand(
        message: (ServerPlayer) -> Component = { Component.literal("This command has been removed").crimson() }
    ) {
        this.removedMessage = message
    }
}