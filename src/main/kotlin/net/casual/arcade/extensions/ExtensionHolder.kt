package net.casual.arcade.extensions

/**
 * Interface class allowing any implementor to
 * have its own [Extension]s.
 *
 * @see Extension
 * @see ExtensionMap
 */
interface ExtensionHolder {
    /**
     * This gets all the extensions that are being held.
     */
    fun getExtensionMap(): ExtensionMap
}