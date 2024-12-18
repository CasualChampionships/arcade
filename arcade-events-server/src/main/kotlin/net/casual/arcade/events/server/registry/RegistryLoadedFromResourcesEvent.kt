/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.registry

import net.casual.arcade.events.common.MissingExecutorEvent
import net.minecraft.core.HolderGetter
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.resources.RegistryOps.RegistryInfoLookup
import net.minecraft.resources.ResourceKey
import java.util.Optional

public data class RegistryLoadedFromResourcesEvent<T>(
    val registry: WritableRegistry<T>,
    val infoLookup: RegistryInfoLookup
): MissingExecutorEvent {
    public fun <T> lookup(registry: ResourceKey<Registry<T>>): Optional<HolderGetter<T>> {
        return this.infoLookup.lookup(registry).map { it.getter }
    }

    public fun <T> lookupOrThrow(registry: ResourceKey<Registry<T>>): HolderGetter<T> {
        return this.lookup(registry).orElseThrow {
            IllegalStateException("Expected registry ${registry.registry()} to be present during dynamic registry load")
        }
    }
}