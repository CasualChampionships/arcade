package net.casual.arcade.extensions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

public abstract class PlayerExtension(
    private val connection: ServerGamePacketListenerImpl
): Extension {
    public val player: ServerPlayer
        get() = this.connection.player
}