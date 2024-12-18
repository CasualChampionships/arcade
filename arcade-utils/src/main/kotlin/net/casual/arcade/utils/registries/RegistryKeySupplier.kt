/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.registries

import net.minecraft.core.Registry
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation

public abstract class RegistryKeySupplier(private val namespace: String) {
    protected fun <T> create(path: String): ResourceKey<Registry<T>> {
        return ResourceKey.createRegistryKey(ResourceLocation.fromNamespaceAndPath(this.namespace, path))
    }
}