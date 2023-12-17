package net.casual.arcade.resources

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