package net.casual.arcade.utils

import com.mojang.serialization.Codec
import com.mojang.serialization.MapCodec
import net.casual.arcade.utils.serialization.EncodedOptionalFieldCodec
import java.util.*

public object CodecUtils {
    public fun <A: Any> Codec<A>.encodedOptionalFieldOf(name: String): MapCodec<Optional<A>> {
        return EncodedOptionalFieldCodec(name, this)
    }

    public fun <A: Any> Codec<A>.encodedOptionalFieldOf(name: String, defaultValue: A): MapCodec<A> {
        return EncodedOptionalFieldCodec(name, this).xmap(
            { o -> o.orElse(defaultValue) },
            { a -> Optional.of(a) }
        )
    }
}