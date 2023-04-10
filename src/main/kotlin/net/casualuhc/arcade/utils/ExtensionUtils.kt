package net.casualuhc.arcade.utils

import net.casualuhc.arcade.extensions.Extension
import net.casualuhc.arcade.extensions.ExtensionHolder

object ExtensionUtils {
    @JvmStatic
    fun ExtensionHolder.addExtension(extension: Extension) {
        this.getExtensionMap().addExtension(extension)
    }

    @JvmStatic
    fun <T: Extension> ExtensionHolder.getExtension(type: Class<T>): T {
        val extension = this.getExtensionMap().getExtension(type)
        if (extension === null) {
            throw IllegalArgumentException("No such extension $type exists for $this")
        }
        return extension
    }

    @JvmStatic
    fun ExtensionHolder.getExtensions(): Collection<Extension> {
        return this.getExtensionMap().getExtensions()
    }
}