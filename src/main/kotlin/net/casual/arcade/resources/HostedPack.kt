package net.casual.arcade.resources

import net.minecraft.network.chat.Component

/**
 * This holds all the data for a [ReadablePack] that
 * is being hosted on a pack host.
 *
 * @param pack The readable pack that is being hosted.
 * @param url The URL that it is being hosted at.
 * @param hash The hash of the [pack].
 */
public data class HostedPack(
    /**
     * The readable pack that is being hosted.
     */
    val pack: ReadablePack,
    /**
     * The URL that it is being hosted at.
     */
    val url: String,
    /**
     * The hash of the [pack].
     */
    val hash: String
) {
    /**
     * This converts the [HostedPack] to [PackInfo] to be able
     * to be sent to players on the server.
     *
     * @param required Whether the pack should be required for the player.
     * @param prompt The prompt given to the player.
     * @return The pack info.
     * @see PackInfo
     */
    public fun toPackInfo(required: Boolean = false, prompt: Component? = null): PackInfo {
        return PackInfo(this.url, this.hash, required, prompt)
    }
}