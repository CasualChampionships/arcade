package net.casual.arcade.utils.codec

import com.mojang.datafixers.kinds.App
import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.util.codec.SortedRecordCodecBuilderInstance
import java.util.function.Function

public object SortedRecordCodecBuilder {
    public fun <O> instance(): SortedRecordCodecBuilderInstance<O> {
        return SortedRecordCodecBuilderInstance<O>()
    }

    public fun <O> mapCodec(builder: Function<SortedRecordCodecBuilderInstance<O>, out App<RecordCodecBuilder.Mu<O>, O>>): MapCodec<O> {
        return RecordCodecBuilder.build(builder.apply(this.instance()))
    }

    public fun <O> create(builder: Function<SortedRecordCodecBuilderInstance<O>, out App<RecordCodecBuilder.Mu<O>, O>>): Codec<O> {
        return this.mapCodec(builder).codec()
    }
}