/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.pack.generation.utils

import net.casual.arcade.pack.generation.PackDefinition
import net.casual.arcade.pack.host.PackHost

public fun PackHost.add(definition: PackDefinition): PackHost.HostedPackRef {
    return this.add(definition.build())
}
