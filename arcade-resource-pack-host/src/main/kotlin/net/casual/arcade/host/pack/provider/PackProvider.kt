/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.pack.provider

import net.casual.arcade.host.pack.ReadablePack

public interface PackProvider {
    /**
     * This is the name of the pack, no file extensions.
     *
     * @param name The name of the pack
     * @return The readable pack, null if not provided.
     */
    public fun get(name: String): ReadablePack?

    public companion object {
        public fun of(pack: ReadablePack): PackProvider {
            return object: PackProvider {
                override fun get(name: String): ReadablePack? {
                    return if (name == pack.name) pack else null
                }
            }
        }

        public fun of(vararg packs: ReadablePack): PackProvider {
            return object: PackProvider {
                override fun get(name: String): ReadablePack? {
                    return packs.find { name == it.name }
                }
            }
        }
    }
}