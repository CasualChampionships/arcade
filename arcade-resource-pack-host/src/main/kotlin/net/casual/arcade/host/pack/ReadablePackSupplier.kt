/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.host.pack

import net.casual.arcade.host.PackHost

/**
 * This interface provides a method for getting
 * multiple [ReadablePack]s to supply to a [PackHost].
 *
 * @see PackHost
 * @see ReadablePack
 */
public fun interface ReadablePackSupplier {
    /**
     * This gets all the currently available packs.
     *
     * @return The available [ReadablePack]s.
     */
    public fun getPacks(): Iterable<ReadablePack>
}