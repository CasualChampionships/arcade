/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization

import com.mojang.serialization.DynamicOps
import net.minecraft.core.HolderLookup

public fun <T> HolderLookup.Provider?.createSerializationContext(ops: DynamicOps<T>): DynamicOps<T> {
    return if (this != null) this.createSerializationContext(ops) else ops
}