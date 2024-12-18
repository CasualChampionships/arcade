/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.registries

import com.mojang.serialization.Lifecycle
import net.minecraft.core.MappedRegistry
import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey

public abstract class RegistrySupplier {
    private val loaders = ArrayList<() -> Unit>()

    public fun load() {
        for (load in this.loaders) {
            load.invoke()
        }
        this.loaders.clear()
    }

    protected fun <T> create(key: ResourceKey<Registry<T>>, bootstrap: (Registry<T>) -> Unit): Registry<T> {
        val registry = MappedRegistry(key, Lifecycle.stable(), false)
        this.loaders.add { bootstrap.invoke(registry) }
        return registry
    }
}