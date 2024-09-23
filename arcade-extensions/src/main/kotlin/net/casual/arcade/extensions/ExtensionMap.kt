package net.casual.arcade.extensions

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

/**
 * This class keeps a map of all [Extension]s
 */
public class ExtensionMap {
    private val extensions = Object2ObjectOpenHashMap<Class<out Extension>, Extension>()

    public fun add(extension: Extension) {
        this.extensions[extension::class.java] = extension
    }

    public fun <T: Extension> get(type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return this.extensions[type] as? T
    }

    public fun all(): Collection<Extension> {
        return this.extensions.values
    }
}