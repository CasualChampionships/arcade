package net.casual.arcade.extensions

/**
 * This class keeps a map of all [Extension]s
 */
public class ExtensionMap {
    private val extensions = HashMap<Class<out Extension>, Extension>()

    public fun addExtension(extension: Extension) {
        this.extensions[extension::class.java] = extension
    }

    public fun getExtensions(): Collection<Extension> {
        return this.extensions.values
    }

    public fun <T: Extension> getExtension(type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return this.extensions[type] as? T
    }
}