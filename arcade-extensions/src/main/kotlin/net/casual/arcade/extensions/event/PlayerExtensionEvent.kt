/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.PlayerExtension
import net.minecraft.server.level.ServerPlayer
import net.casual.arcade.extensions.utils.addExtension as addExtensionNew
import net.casual.arcade.extensions.utils.getExtension as getExtensionNew

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
        this.player.addExtensionNew(extension)
    }

    public fun addExtension(provider: (ServerPlayer) -> Extension) {
        this.addExtension(provider.invoke(this.player))
    }

    public companion object {
        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.addExtension(extension)",
                "net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.addExtension",
                "net.casual.arcade.extensions.utils.addExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        public fun ServerPlayer.addExtension(extension: Extension) {
            this.addExtensionNew(extension)
        }

        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.getExtension(type)",
                "net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.getExtension",
                "net.casual.arcade.extensions.utils.getExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        public fun <T: Extension> ServerPlayer.getExtension(type: Class<T>): T {
            return this.getExtensionNew(type)
        }

        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.getExtension<T>()",
                "net.casual.arcade.extensions.event.PlayerExtensionEvent.Companion.getExtension",
                "net.casual.arcade.extensions.utils.getExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        public inline fun <reified T: Extension> ServerPlayer.getExtension(): T {
            return this.getExtensionNew<T>()
        }
    }
}