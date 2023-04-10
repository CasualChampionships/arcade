package net.casualuhc.arcade.utils

import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.extensions.ExtensionHolder
import net.casualuhc.arcade.utils.ExtensionUtils.addExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtension
import net.casualuhc.arcade.utils.ExtensionUtils.getExtensions
import net.minecraft.server.level.ServerLevel

@Suppress("unused")
object LevelUtils {
    @JvmStatic
    fun ServerLevel.addExtension(extension: Extension) {
        (this as ExtensionHolder).addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> ServerLevel.getExtension(type: Class<T>): T {
        return (this as ExtensionHolder).getExtension(type)
    }

    @JvmStatic
    fun ServerLevel.getExtensions(): Collection<Extension> {
        return (this as ExtensionHolder).getExtensions()
    }
}