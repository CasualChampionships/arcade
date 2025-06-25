/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.extensions

import net.minecraft.world.level.storage.ValueInput
import net.minecraft.world.level.storage.ValueOutput

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
        public fun ExtensionHolder.deserialize(input: ValueInput) {
            for (extension in this.all()) {
                if (extension is DataExtension) {
                    val child = input.childOrEmpty(extension.getId().toString())
                    extension.deserialize(child)
                }
            }
        }

        @JvmStatic
        public fun ExtensionHolder.serialize(output: ValueOutput) {
            for (extension in this.all()) {
                if (extension is DataExtension) {
                    val child = output.child(extension.getId().toString())
                    extension.serialize(child)
                }
            }
        }
    }
}