/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.player.PlayerEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.PlayerExtension
import net.casual.arcade.extensions.event.EntityExtensionEvent.Companion.addExtension
import net.casual.arcade.extensions.event.EntityExtensionEvent.Companion.getExtension
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity

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
            (this as Entity).addExtension(extension)
        }

        public fun <T: Extension> ServerPlayer.getExtension(type: Class<T>): T {
            return (this as Entity).getExtension(type)
        }

        public inline fun <reified T: Extension> ServerPlayer.getExtension(): T {
            return (this as Entity).getExtension<T>()
        }
    }
}