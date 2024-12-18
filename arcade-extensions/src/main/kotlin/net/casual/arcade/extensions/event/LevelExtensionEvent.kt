/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.level.LevelEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.minecraft.server.level.ServerLevel

public data class LevelExtensionEvent(
    override val level: ServerLevel
): LevelEvent, ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.level.addExtension(extension)
    }

    public fun addExtension(provider: (ServerLevel) -> Extension) {
        this.addExtension(provider.invoke(this.level))
    }

    public companion object {
        @JvmStatic
        public fun ServerLevel.addExtension(extension: Extension) {
            (this as ExtensionHolder).add(extension)
        }

        @JvmStatic
        public fun <T: Extension> ServerLevel.getExtension(type: Class<T>): T {
            return (this as ExtensionHolder).get(type)
        }

        public inline fun <reified T: Extension> ServerLevel.getExtension(): T {
            return this.getExtension(T::class.java)
        }
    }
}