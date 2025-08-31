/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.kotlin

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.JsonBuilder
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.SerializersModuleBuilder
import kotlinx.serialization.modules.SerializersModuleCollector
import kotlinx.serialization.modules.contextual
import net.minecraft.core.HolderLookup
import kotlin.reflect.KClass

@Suppress("UnusedReceiverParameter", "FunctionName")
public fun JsonBuilder.CodecSerializersModule(
    lookup: HolderLookup.Provider,
    block: CodecSerializersModuleBuilder.() -> Unit
): SerializersModule {
    return SerializersModule {
        val builder = CodecSerializersModuleBuilder(this, lookup.createSerializationContext(KJsonOps))
        builder.block()
    }
}

@Suppress("UnusedReceiverParameter", "FunctionName")
public fun JsonBuilder.CodecSerializersModule(
    block: CodecSerializersModuleBuilder.() -> Unit
): SerializersModule {
    return SerializersModule {
        val builder = CodecSerializersModuleBuilder(this, KJsonOps)
        builder.block()
    }
}

@OptIn(ExperimentalSerializationApi::class)
public class CodecSerializersModuleBuilder(
    private val inner: SerializersModuleBuilder,
    private val ops: DynamicOps<JsonElement>
): SerializersModuleCollector by inner {
    public inline fun <reified T: Any> contextual(codec: Codec<T>) {
        this.contextual(T::class, codec)
    }

    public fun <T: Any> contextual(clazz: KClass<T>, codec: Codec<T>) {
        this.inner.contextual(clazz, KJsonCodecSerializer(codec, this.ops))
    }

    public inline fun <reified T: Any> contextual(serializer: KSerializer<T>) {
        this.contextual(T::class, serializer)
    }
}