/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.resources.pack

import net.minecraft.network.protocol.common.ServerboundResourcePackPacket
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.*
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.ACCEPTED as ACCEPTED_PACK
import net.minecraft.network.protocol.common.ServerboundResourcePackPacket.Action.DECLINED as DECLINED_PACK

/**
 * This enum represents a player's server-side
 * resource pack status.
 */
public enum class PackStatus {
    /**
     * Waiting for the player's response to receiving the
     * server-side resource pack request.
     *
     * This will be followed by either [ACCEPTED] or [DECLINED].
     */
    WAITING,

    /**
     * The player has accepted to download the server-side
     * resource pack and is now downloading it.
     *
     * This will be followed by either [SUCCESS] or [FAILED].
     */
    ACCEPTED,

    /**
     * The player has declined the server-side resource
     * pack and will not download it.
     */
    DECLINED,

    /**
     * The player has successfully loaded the server-side
     * resource pack.
     *
     * This may be followed by [REMOVED] if the server pops
     * the pack.
     */
    SUCCESS,

    /**
     * The player has failed to load the server-side
     * resource pack.
     */
    FAILED,

    /**
     * The player has successfully removed the pack,
     * after the server has popped it from the client.
     */
    REMOVED;

    /**
     * Checks whether the player has successfully loaded
     * the pack on their client.
     *
     * @return Whether the player has loaded the pack.
     */
    public fun hasLoadedPack(): Boolean {
        return this == SUCCESS
    }

    /**
     * Checks whether the player is currently loading the
     * pack on their client. This **doesn't** mean that
     * it will be successful.
     *
     * @return Whether the player is loading the pack.
     */
    public fun isLoadingPack(): Boolean {
        return this == WAITING || this == ACCEPTED
    }

    /**
     * Checks whether the player has declined the pack
     * on their client. This means they do not have the
     * pack available on their client.
     *
     * @return Whether the player has declined the pack.
     */
    public fun hasDeclinedPack(): Boolean {
        return this == DECLINED
    }

    /**
     * Checks whether the player has failed to download the
     * pack. This means they accepted the pack, but something
     * went wrong when downloading / validating the pack.
     *
     * This usually happens when the [PackInfo.hash] doesn't
     * match the downloaded pack.
     *
     * @return Whether the player has failed to download the pack.
     */
    public fun hasFailedToLoadPack(): Boolean {
        return this == FAILED
    }

    public companion object {
        /**
         * This converts a [ServerboundResourcePackPacket.Action] to a [PackStatus].
         *
         * @return The corresponding pack status.
         */
        @JvmStatic
        public fun ServerboundResourcePackPacket.Action.toPackStatus(): PackStatus {
            return when (this) {
                SUCCESSFULLY_LOADED -> SUCCESS
                FAILED_DOWNLOAD, INVALID_URL, FAILED_RELOAD -> FAILED
                ACCEPTED_PACK -> ACCEPTED
                DECLINED_PACK -> DECLINED
                DISCARDED -> REMOVED
                else -> WAITING
            }
        }
    }
}