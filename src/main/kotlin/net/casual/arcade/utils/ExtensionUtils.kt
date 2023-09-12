package net.casual.arcade.utils

import net.casual.arcade.extensions.DataExtension
import net.casual.arcade.extensions.Extension
import net.casual.arcade.extensions.ExtensionHolder
import net.minecraft.nbt.CompoundTag

public object ExtensionUtils {
    @JvmStatic
    public fun ExtensionHolder.deserialize(tag: CompoundTag) {
        for (extension in this.getExtensions()) {
            if (extension is DataExtension) {
                val data = tag[extension.getName()]
                if (data != null) {
                    extension.deserialize(data)
                }
            }
        }
    }

    @JvmStatic
    public fun ExtensionHolder.serialize(tag: CompoundTag) {
        for (extension in this.getExtensions()) {
            if (extension is DataExtension) {
                tag.put(extension.getName(), extension.serialize())
            }
        }
    }

    @JvmStatic
    public fun ExtensionHolder.addExtension(extension: Extension) {
        this.getExtensionMap().addExtension(extension)
    }

    @JvmStatic
    public fun <T: Extension> ExtensionHolder.getExtension(type: Class<T>): T {
        val extension = this.getExtensionMap().getExtension(type)
        if (extension === null) {
            throw IllegalArgumentException("No such extension $type exists for $this")
        }
        return extension
    }

    @JvmStatic
    public fun ExtensionHolder.getExtensions(): Collection<Extension> {
        return this.getExtensionMap().getExtensions()
    }
}