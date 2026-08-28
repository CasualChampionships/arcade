/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import net.minecraft.core.HolderLookup
import net.minecraft.resources.RegistryOps
import java.util.*

public typealias TypedValueOutputStorer<T> = (Codec<T>, T) -> Unit

public typealias TypedValueInputProvider<T> = (Codec<T>) -> Optional<T>

public fun <T: Any> HolderLookup.Provider?.createSerializationContext(ops: DynamicOps<T>): DynamicOps<T> {
    return this?.createSerializationContext(ops) ?: ops
}

public fun <T: Any> RegistryOps.RegistryInfoLookup?.createSerializationContext(ops: DynamicOps<T>): DynamicOps<T> {
    return if (this == null) ops else RegistryOps.create(ops, this)
}