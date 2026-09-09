/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions.event

import net.casual.arcade.extensions.Extension
import net.minecraft.server.MinecraftServer
import net.minecraft.world.scores.PlayerTeam
import net.casual.arcade.extensions.utils.addExtension as addExtensionNew
import net.casual.arcade.extensions.utils.getExtension as getExtensionNew

public data class TeamExtensionEvent(
    val team: PlayerTeam,
    val server: MinecraftServer
): ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.team.addExtensionNew(extension)
    }
}