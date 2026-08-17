/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.host

import java.io.InputStream

/**
 * An interface for providing a resource pack to
 * a [PackHost].
 *
 * @see PackHost
 */
public interface ReadablePack {
    /**
     * The name of the pack.
     * This should not contain any file extensions.
     */
    public val name: String

    /**
     * This streams the contents of the [ReadablePack].
     *
     * @return The [InputStream] for the pack.
     */
    public fun stream(): InputStream

    /**
     * Checks whether the pack is currently readable.
     *
     * @return Whether the pack is readable.
     */
    public fun readable(): Boolean {
        return true
    }

    /**
     * This gets the number of bytes of the [ReadablePack].
     * This may return 0 if the exact number is not known.
     *
     * @return The size of the pack in bytes.
     */
    public fun length(): Long {
        return 0
    }

    /**
     * This gets the SHA-1 hash of the [ReadablePack], if it is already known.
     * If this returns `null` the host will calculate the hash itself.
     *
     * @return The hash of the pack, or `null` if it is not known.
     */
    public fun hash(): String? {
        return null
    }

    public companion object {
        /**
         * Creates a single use [ReadablePack] with a given [name] and [stream].
         *
         * @param name The name of the pack.
         * @param stream The stream for the pack.
         * @return The readable pack.
         */
        public fun of(name: String, stream: InputStream): ReadablePack {
            return object: ReadablePack {
                private var streamed = false

                override val name: String
                    get() = name

                override fun stream(): InputStream {
                    if (!this.streamed) {
                        this.streamed = true
                        return stream
                    }
                    throw UnsupportedOperationException("Tried to read single use pack multiple times")
                }
            }
        }
    }
}