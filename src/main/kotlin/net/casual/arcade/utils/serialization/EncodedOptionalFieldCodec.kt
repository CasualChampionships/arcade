package net.casual.arcade.utils.serialization

import com.mojang.serialization.Codec
import com.mojang.serialization.DynamicOps
import com.mojang.serialization.RecordBuilder
import com.mojang.serialization.codecs.OptionalFieldCodec
import java.util.*

public class EncodedOptionalFieldCodec<A>(
    private val name: String,
    private val elementCodec: Codec<A>
): OptionalFieldCodec<A>(name, elementCodec, true) {
    override fun <T> encode(
        input: Optional<A>,
        ops: DynamicOps<T>,
        prefix: RecordBuilder<T>
    ): RecordBuilder<T> {
        if (input.isPresent) {
            return prefix.add(this.name, this.elementCodec.encodeStart(ops, input.get()))
        }
        // We encode this as empty
        return prefix.add(name, ops.empty())
    }
}