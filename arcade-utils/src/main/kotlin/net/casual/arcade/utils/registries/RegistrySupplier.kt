/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.registries

import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.core.Registry
import net.minecraft.core.WritableRegistry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

public abstract class RegistrySupplier {
    private val loaders = ArrayList<() -> Unit>()

    public fun load() {
        for (load in this.loaders) {
            load.invoke()
        }
        this.loaders.clear()
    }

    protected fun <T> create(key: ResourceKey<Registry<T>>, bootstrap: (Registry<T>) -> Unit): Registry<T> {
        return this.register(FabricRegistryBuilder.createSimple(key), bootstrap)
    }

    protected fun <T> createDefaulted(key: ResourceKey<Registry<T>>, default: ResourceLocation, bootstrap: (Registry<T>) -> Unit): Registry<T> {
        return this.register(FabricRegistryBuilder.createDefaulted(key, default), bootstrap)
    }

    protected fun <T, R: WritableRegistry<T>> register(builder: FabricRegistryBuilder<T, R>, bootstrap: (Registry<T>) -> Unit): R {
        val registry = builder.buildAndRegister()
        this.loaders.add { bootstrap.invoke(registry) }
        return registry
    }

    @Deprecated("Use fabric's registry build instead")
    protected fun <T> registerRegistry(registry: Registry<T>, bootstrap: (Registry<T>) -> Unit): Registry<T> {
        @Suppress("UNCHECKED_CAST")
        Registry.register(
            BuiltInRegistries.REGISTRY as Registry<Registry<*>>,
            registry.key() as ResourceKey<Registry<*>>,
            registry
        )
        this.loaders.add { bootstrap.invoke(registry) }
        return registry
    }
}