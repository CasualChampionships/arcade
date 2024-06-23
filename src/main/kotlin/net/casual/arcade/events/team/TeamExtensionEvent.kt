package net.casual.arcade.events.team

import net.casual.arcade.events.core.ExtensionEvent
import net.casual.arcade.extensions.Extension
import net.casual.arcade.utils.TeamUtils.addExtension
import net.minecraft.world.scores.PlayerTeam

public data class TeamExtensionEvent(
    val team: PlayerTeam
): ExtensionEvent {
    override fun addExtension(extension: Extension) {
        this.team.addExtension(extension)
    }
}