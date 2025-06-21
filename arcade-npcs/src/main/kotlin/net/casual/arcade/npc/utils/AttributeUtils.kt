/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.npc.utils

import net.casual.arcade.npc.mixins.AttributeSupplierAccessor
import net.minecraft.world.entity.ai.attributes.AttributeSupplier

public object AttributeUtils {
    public fun AttributeSupplier.toBuilder(): AttributeSupplier.Builder {
        val builder = AttributeSupplier.builder()
        val instances = (this as AttributeSupplierAccessor).instances
        for ((attribute, instance) in instances) {
            builder.add(attribute, instance.baseValue)
        }
        return builder
    }
}