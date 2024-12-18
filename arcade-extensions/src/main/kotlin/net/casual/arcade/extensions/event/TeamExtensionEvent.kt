/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.casual.arcade.extensions.ExtensionHolder.Companion.add
import net.casual.arcade.extensions.ExtensionHolder.Companion.get
import net.minecraft.world.scores.PlayerTeam

public data class TeamExtensionEvent(
    val team: PlayerTeam
): ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.team.addExtension(extension)
    }

    public companion object {
        public fun PlayerTeam.addExtension(extension: Extension) {
            (this as ExtensionHolder).add(extension)
        }

        public fun <T: Extension> PlayerTeam.getExtension(type: Class<T>): T {
            return (this as ExtensionHolder).get(type)
        }

        public inline fun <reified T: Extension> PlayerTeam.getExtension(): T {
            return this.getExtension(T::class.java)
        }
    }
}