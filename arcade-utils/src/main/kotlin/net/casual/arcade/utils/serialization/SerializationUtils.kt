/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization

import com.mojang.serialization.DynamicOps
import net.minecraft.core.HolderLookup

public fun <T: Any> HolderLookup.Provider?.createSerializationContext(ops: DynamicOps<T>): DynamicOps<T> {
    return this?.createSerializationContext(ops) ?: ops
}