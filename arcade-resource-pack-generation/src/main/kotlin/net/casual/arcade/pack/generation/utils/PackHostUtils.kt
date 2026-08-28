/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation.utils

import net.casual.arcade.pack.generation.PackDefinition
import net.casual.arcade.pack.host.HostedPackRef
import net.casual.arcade.pack.host.PackHost

/**
 * Hosts a given pack [definition].
 *
 * The definition is only built if it is not already being hosted.
 *
 * @param definition The pack definition to host.
 * @return A reference to the pack being hosted.
 */
public fun PackHost.add(definition: PackDefinition): HostedPackRef {
    return this.add(definition.uuid, definition::build)
}

/**
 * Stops hosting a given pack [definition].
 *
 * @param definition The pack definition to stop hosting.
 * @return Whether the pack was removed.
 */
public fun PackHost.remove(definition: PackDefinition): Boolean {
    return this.remove(definition.uuid)
}
