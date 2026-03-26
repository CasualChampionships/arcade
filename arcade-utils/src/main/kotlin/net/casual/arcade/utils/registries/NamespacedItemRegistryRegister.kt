/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.registries

import net.casual.arcade.utils.toKey
import net.minecraft.core.Registry
import net.minecraft.core.registries.BuiltInRegistries
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.world.item.BlockItem
import net.minecraft.world.item.Item
import net.minecraft.world.item.Item.Properties

public class NamespacedItemRegistryRegister(
    private val namespace: (String) -> Identifier
) {
    public fun register(path: String, provider: (Properties) -> Item): Item {
        return this.register(path, Properties(), provider)
    }

    public fun register(path: String, properties: Properties, provider: (Properties) -> Item): Item {
        val key = this.namespace.invoke(path).toKey(Registries.ITEM)
        val item = provider.invoke(properties.setId(key))
        // fabric-api does this for us, but we'll do it just in case anyway
        if (item is BlockItem) {
            item.registerBlocks(Item.BY_BLOCK, item)
        }
        return Registry.register(BuiltInRegistries.ITEM, key, item)
    }

    public operator fun invoke(path: String, provider: (Properties) -> Item): Item {
        return this.register(path, provider)
    }

    public operator fun invoke(path: String, properties: Properties, provider: (Properties) -> Item): Item {
        return this.register(path, properties, provider)
    }
}