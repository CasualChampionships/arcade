/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.events.server.registry

import net.casual.arcade.events.common.MissingExecutorEvent
import net.casual.arcade.events.server.mixins.registry.RegistryLoadTaskAccessor
import net.minecraft.core.HolderGetter
import net.minecraft.core.RegistrationInfo
import net.minecraft.core.Registry
import net.minecraft.resources.RegistryOps.RegistryInfoLookup
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceManagerRegistryLoadTask
import java.util.Optional

public data class RegistryLoadedFromResourcesEvent<T: Any>(
    val task: ResourceManagerRegistryLoadTask<T>,
    val context: RegistryInfoLookup
): MissingExecutorEvent {
    public fun <T: Any> lookup(registry: ResourceKey<Registry<T>>): Optional<HolderGetter<T>> {
        return this.context.lookup(registry).map { it.getter }
    }

    public fun <T: Any> lookupOrThrow(registry: ResourceKey<Registry<T>>): HolderGetter<T> {
        return this.lookup(registry).orElseThrow {
            IllegalStateException("Expected registry ${registry.registry()} to be present during dynamic registry load")
        }
    }

    @Suppress("UNCHECKED_CAST")
    public fun registryKey(): ResourceKey<out Registry<T>> {
        return (this.task as RegistryLoadTaskAccessor<T>).arcade_accessRegistry().key()
    }

    @Suppress("UNCHECKED_CAST")
    public fun register(key: ResourceKey<T>, value: T, info: RegistrationInfo = RegistrationInfo.BUILT_IN) {
        this.task.createRegistryInfo()
        this.task as RegistryLoadTaskAccessor<T>
        synchronized(this.task.arcade_accessRegistryWriteLock()) {
            val registry = this.task.arcade_accessRegistry()
            registry.register(key, value, info)
        }
    }
}