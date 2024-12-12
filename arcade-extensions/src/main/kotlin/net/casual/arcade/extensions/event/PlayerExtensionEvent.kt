package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.casual.arcade.extensions.PlayerExtension
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
): PlayerEvent, ExtensionEvent {
    /**
     * This adds an extension to the player.
     *
     * @param extension The extension to add.
     * @see PlayerExtension
     */
    override fun addExtension(extension: Extension) {
        this.player.addExtension(extension)
    }

    public fun addExtension(provider: (ServerPlayer) -> Extension) {
        this.addExtension(provider.invoke(this.player))
    }

    public companion object {
        public fun ServerPlayer.addExtension(extension: Extension) {
            (this as ExtensionHolder).add(extension)
        }

        public fun <T: Extension> ServerPlayer.getExtension(type: Class<T>): T {
            return (this as ExtensionHolder).get(type)
        }

        public inline fun <reified T: Extension> ServerPlayer.getExtension(): T {
            return this.getExtension(T::class.java)
        }
    }
}