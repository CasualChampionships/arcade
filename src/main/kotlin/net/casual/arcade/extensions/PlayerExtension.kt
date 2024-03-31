package net.casual.arcade.extensions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl

/**
 * This is an abstract class for all player extensions.
 *
 * While it is not strictly necessary that player extensions
 * must extend this class, it is recommended to do so, as
 * it holds a reference to the [connection] instead of the player.
 * This handles the case where the player instance is replaced
 * as a result of the player respawning.
 *
 * @param connection The connection of the player.
 * @see Extension
 */
public abstract class PlayerExtension(
    private val connection: ServerGamePacketListenerImpl
): Extension {
    /**
     * The player this extension is attached to.
     */
    public val player: ServerPlayer
        get() = this.connection.player

    /**
     * Constructs a new player extension with the given player.
     *
     * @param player The player this extension is attached to.
     */
    public constructor(player: ServerPlayer): this(player.connection)
}