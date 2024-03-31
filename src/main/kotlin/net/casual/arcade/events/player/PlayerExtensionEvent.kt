package net.casual.arcade.events.player

import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.utils.PlayerUtils.addExtension
import net.minecraft.server.level.ServerPlayer

/**
 * This event is broadcast when a player is created.
 * If you have any extensions that you want to add to the player,
 * you must hook into this event and register them here.
 */
public data class PlayerExtensionEvent(
    /**
     * The [player] that is tied to the event.
     */
    override val player: ServerPlayer
): PlayerEvent {
    /**
     * This adds an extension to the player.
     *
     * @param extension The extension to add.
     * @see PlayerExtension
     */
    public fun addExtension(extension: Extension) {
        this.player.addExtension(extension)
    }
}