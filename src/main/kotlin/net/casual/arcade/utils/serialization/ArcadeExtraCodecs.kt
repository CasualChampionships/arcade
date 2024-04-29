package net.casual.arcade.utils.serialization

import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import java.nio.file.Path
import kotlin.io.path.pathString

public object ArcadeExtraCodecs {
    public val PATH: Codec<Path> = Codec.STRING.xmap(Path::of, Path::pathString)

    public inline fun <reified E: Enum<E>> enum(
        mapper: (E) -> String = { it.name.lowercase() }
    ): Codec<E> {
        val map = HashBiMap.create<String, E>()
        for (constant in E::class.java.enumConstants) {
            map[mapper(constant)] = constant
        }
        val inverse = map.inverse()
        return Codec.STRING.xmap(map::get, inverse::get)
    }
}