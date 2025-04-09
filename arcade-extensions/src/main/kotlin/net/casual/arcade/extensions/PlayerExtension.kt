/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.server.level.ServerPlayer
import net.minecraft.server.network.ServerGamePacketListenerImpl
import net.minecraft.world.entity.Entity

/**
 * This is an abstract class for all player extensions.
 *
 * While it is not strictly necessary that player extensions
 * must extend this class, it is recommended to do so, as
 * it holds a reference to the [connection] instead of the player.
 * This handles the case where the player instance is replaced
 * as a result of the player respawning.
 *
 * By default, the same instance of the extension is kept for
 * new instances of [ServerPlayer] that are created, these can
 * be either from the player respawning, or the player going through
 * something like the end portal.
 *
 * You can also implement [DataExtension].
 *
 * @param connection The connection of the player.
 * @see Extension
 */
public abstract class PlayerExtension(
    private val connection: ServerGamePacketListenerImpl
): TransferableEntityExtension {
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

    public open fun transfer(player: ServerPlayer, respawned: Boolean): Extension {
        return this
    }

    final override fun transfer(entity: Entity, respawned: Boolean): Extension {
        return this.transfer(entity as ServerPlayer, respawned)
    }
}