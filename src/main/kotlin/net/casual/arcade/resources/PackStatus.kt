package net.casual.arcade.resources

import net.minecraft.network.protocol.game.ServerboundResourcePackPacket

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
     */
    SUCCESS,

    /**
     * The player has failed to load the server-side
     * resource pack.
     */
    FAILED;

    public companion object {
        /**
         * This converts a [ServerboundResourcePackPacket.Action] to a [PackStatus].
         *
         * @return The corresponding pack status.
         */
        public fun ServerboundResourcePackPacket.Action.toPackStatus(): PackStatus {
            return when (this) {
                ServerboundResourcePackPacket.Action.SUCCESSFULLY_LOADED -> SUCCESS
                ServerboundResourcePackPacket.Action.FAILED_DOWNLOAD -> FAILED
                ServerboundResourcePackPacket.Action.ACCEPTED -> ACCEPTED
                ServerboundResourcePackPacket.Action.DECLINED -> DECLINED
                else -> WAITING
            }
        }
    }
}