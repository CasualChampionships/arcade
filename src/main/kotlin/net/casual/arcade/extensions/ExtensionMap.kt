package net.casual.arcade.extensions

/**
 * This class keeps a map of all [Extension]s
 */
class ExtensionMap {
    private val extensions = HashMap<Class<out Extension>, Extension>()

    fun addExtension(extension: Extension) {
        this.extensions[extension::class.java] = extension
    }

    fun getExtensions(): Collection<Extension> {
        return this.extensions.values
    }

    fun <T: Extension> getExtension(type: Class<T>): T? {
        @Suppress("UNCHECKED_CAST")
        return this.extensions[type] as? T
    }
}