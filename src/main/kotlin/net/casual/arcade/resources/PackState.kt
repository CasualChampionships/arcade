package net.casual.arcade.resources

/**
 * This class contains the information for the player's
 * current resource pack state.
 *
 * Containing the pack that they were last sent and their
 * status on that last sent pack.
 *
 * @param pack The pack that the player was last sent.
 * @param status The current status of the pack.
 */
public class PackState(
    /**
     * The pack that the player was last sent.
     */
    public val pack: PackInfo,
    /**
     * The current status of the pack.
     */
    private val status: PackStatus
) {
    /**
     * Checks whether the player has successfully loaded
     * the pack on their client.
     *
     * @return Whether the player has loaded the pack.
     */
    public fun hasLoadedPack(): Boolean {
        return this.status == PackStatus.SUCCESS
    }

    /**
     * Checks whether the player is currently loading the
     * pack on their client. This **doesn't** mean that
     * it will be successful.
     *
     * @return Whether the player is loading the pack.
     */
    public fun isLoadingPack(): Boolean {
        return this.status == PackStatus.WAITING || this.status == PackStatus.ACCEPTED
    }

    /**
     * Checks whether the player has declined the pack
     * on their client. This means they do not have the
     * pack available on their client.
     *
     * @return Whether the player has declined the pack.
     */
    public fun hasDeclinedPack(): Boolean {
        return this.status == PackStatus.DECLINED
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
        return this.status == PackStatus.FAILED
    }
}