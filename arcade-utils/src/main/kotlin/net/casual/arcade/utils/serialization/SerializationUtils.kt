package net.casual.arcade.utils.serialization

import com.mojang.serialization.DynamicOps
import net.minecraft.core.HolderLookup

public fun <T> HolderLookup.Provider?.createSerializationContext(ops: DynamicOps<T>): DynamicOps<T> {
    return if (this != null) this.createSerializationContext(ops) else ops
}