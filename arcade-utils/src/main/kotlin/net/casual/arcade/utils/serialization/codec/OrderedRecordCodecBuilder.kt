/*
 * Copyright (c) 2025 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.codec

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import java.util.function.Function

@Deprecated("Use RecordCodecBuilder instead", ReplaceWith("RecordCodecBuilder"))
public object OrderedRecordCodecBuilder {
    @Deprecated("Use RecordCodecBuilder instead", ReplaceWith(
        "RecordCodecBuilder.mapCodec(builder)",
        "com.mojang.serialization.codecs.RecordCodecBuilder"
    ))
    public fun <O> mapCodec(builder: Function<RecordCodecBuilder.Instance<O>, out App<RecordCodecBuilder.Mu<O>, O>>): MapCodec<O> {
        return RecordCodecBuilder.mapCodec(builder)
    }

    @Deprecated("Use RecordCodecBuilder instead", ReplaceWith(
        "RecordCodecBuilder.create(builder)",
        "com.mojang.serialization.codecs.RecordCodecBuilder"
    ))
    public fun <O> create(builder: Function<RecordCodecBuilder.Instance<O>, out App<RecordCodecBuilder.Mu<O>, O>>): Codec<O> {
        return RecordCodecBuilder.create(builder)
    }
}