package net.casualuhc.arcade.utils

import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.extensions.ExtensionHolder
import net.casualuhc.arcade.utils.ExtensionUtils.addExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.world.scores.PlayerTeam
import net.minecraft.world.scores.Team

@Suppress("unused")
object TeamUtils {
    @JvmStatic
    fun Team.asPlayerTeam(): PlayerTeam {
        return this as PlayerTeam
    }

    @JvmStatic
    fun PlayerTeam.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> PlayerTeam.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    fun PlayerTeam.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}