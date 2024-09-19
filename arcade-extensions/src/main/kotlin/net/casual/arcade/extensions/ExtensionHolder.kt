package net.casual.arcade.extensions

import net.minecraft.nbt.CompoundTag

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

    public companion object {
        @JvmStatic
        public fun ExtensionHolder.add(extension: Extension) {
            this.getExtensionMap().add(extension)
        }

        @JvmStatic
        public fun <T: Extension> ExtensionHolder.get(type: Class<T>): T {
            return this.getExtensionMap().get(type)
                ?: throw IllegalStateException("No extension $type was registered to $this")
        }

        @JvmStatic
        public fun ExtensionHolder.all(): Collection<Extension> {
            return this.getExtensionMap().all()
        }

        @JvmStatic
        public fun ExtensionHolder.deserialize(tag: CompoundTag) {
            for (extension in this.all()) {
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
            for (extension in this.all()) {
                if (extension is DataExtension) {
                    val serialized = extension.serialize()
                    if (serialized != null) {
                        tag.put(extension.getName(), serialized)
                    }
                }
            }
        }
    }
}