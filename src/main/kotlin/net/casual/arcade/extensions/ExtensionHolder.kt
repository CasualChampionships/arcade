package net.casual.arcade.extensions

/**
 * Interface class allowing any implementor to
 * have its own [Extension]s.
 *
 * @see Extension
 * @see ExtensionMap
 */
public interface ExtensionHolder {
    /**
     * This gets all the extensions that are being held.
     *
     * @return The extension map.
     */
    public fun getExtensionMap(): ExtensionMap
}