/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.events.server.level.LevelEvent
import net.casual.arcade.extensions.Extension
import net.minecraft.server.level.ServerLevel
import net.casual.arcade.extensions.utils.addExtension as addExtensionNew
import net.casual.arcade.extensions.utils.getExtension as getExtensionNew

public data class LevelExtensionEvent(
    override val level: ServerLevel
): LevelEvent, ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.level.addExtensionNew(extension)
    }

    public fun addExtension(provider: (ServerLevel) -> Extension) {
        this.addExtension(provider.invoke(this.level))
    }

    public companion object {
        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.addExtension(extension)",
                "net.casual.arcade.extensions.event.LevelExtensionEvent.Companion.addExtension",
                "net.casual.arcade.extensions.utils.addExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        @JvmStatic
        public fun ServerLevel.addExtension(extension: Extension) {
            this.addExtensionNew(extension)
        }

        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.getExtension(type)",
                "net.casual.arcade.extensions.event.LevelExtensionEvent.Companion.getExtension",
                "net.casual.arcade.extensions.utils.getExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        @JvmStatic
        public fun <T: Extension> ServerLevel.getExtension(type: Class<T>): T {
            return this.getExtensionNew(type)
        }

        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.getExtension<T>()",
                "net.casual.arcade.extensions.event.LevelExtensionEvent.Companion.getExtension",
                "net.casual.arcade.extensions.utils.getExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        public inline fun <reified T: Extension> ServerLevel.getExtension(): T {
            return this.getExtensionNew<T>()
        }
    }
}