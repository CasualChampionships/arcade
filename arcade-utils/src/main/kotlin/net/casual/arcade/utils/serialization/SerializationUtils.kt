/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import net.minecraft.core.HolderLookup
import net.minecraft.resources.Identifier
import net.minecraft.world.level.storage.ValueInput
import java.util.Optional
import java.util.UUID

public typealias TypedValueOutputStorer<T> = (Codec<T>, T) -> Unit

public typealias TypedValueInputProvider<T> = (Codec<T>) -> Optional<T>

public fun <T: Any> HolderLookup.Provider?.createSerializationContext(ops: DynamicOps<T>): DynamicOps<T> {
    return this?.createSerializationContext(ops) ?: ops
}