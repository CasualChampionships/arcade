/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.extensions.Extension
import net.minecraft.world.scores.PlayerTeam
import net.casual.arcade.extensions.utils.addExtension as addExtensionNew
import net.casual.arcade.extensions.utils.getExtension as getExtensionNew

public data class TeamExtensionEvent(
    val team: PlayerTeam
): ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.team.addExtensionNew(extension)
    }

    public companion object {
        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.addExtension(extension)",
                "net.casual.arcade.extensions.event.TeamExtensionEvent.Companion.addExtension",
                "net.casual.arcade.extensions.utils.addExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        public fun PlayerTeam.addExtension(extension: Extension) {
            this.addExtensionNew(extension)
        }

        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.getExtension(type)",
                "net.casual.arcade.extensions.event.TeamExtensionEvent.Companion.getExtension",
                "net.casual.arcade.extensions.utils.getExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        public fun <T: Extension> PlayerTeam.getExtension(type: Class<T>): T {
            return this.getExtensionNew(type)
        }

        @Deprecated(
            "Moved",
            ReplaceWith(
                "this.getExtension<T>()",
                "net.casual.arcade.extensions.event.TeamExtensionEvent.Companion.getExtension",
                "net.casual.arcade.extensions.utils.getExtension"
            ),
            level = DeprecationLevel.ERROR
        )
        public inline fun <reified T: Extension> PlayerTeam.getExtension(): T {
            return this.getExtensionNew<T>()
        }
    }
}