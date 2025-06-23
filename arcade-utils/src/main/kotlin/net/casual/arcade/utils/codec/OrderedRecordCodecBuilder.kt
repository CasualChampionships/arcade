/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.codec

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.util.codec.OrderedRecordCodecBuilderInstance
import java.util.function.Function

public object OrderedRecordCodecBuilder {
    public fun <O> instance(): OrderedRecordCodecBuilderInstance<O> {
        return OrderedRecordCodecBuilderInstance<O>()
    }

    public fun <O> mapCodec(builder: Function<OrderedRecordCodecBuilderInstance<O>, out App<RecordCodecBuilder.Mu<O>, O>>): MapCodec<O> {
        return RecordCodecBuilder.build(builder.apply(this.instance()))
    }

    public fun <O> create(builder: Function<OrderedRecordCodecBuilderInstance<O>, out App<RecordCodecBuilder.Mu<O>, O>>): Codec<O> {
        return this.mapCodec(builder).codec()
    }
}