package net.casual.arcade.utils.serialization

import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.minecraft.Util
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.phys.Vec2
import java.nio.file.Path
import java.util.*
import kotlin.enums.enumEntries
import kotlin.io.path.pathString

public object ArcadeExtraCodecs {
    public val PATH: Codec<Path> = Codec.STRING.xmap(Path::of, Path::pathString)
    public val TIME_DURATION: Codec<MinecraftTimeDuration> = Codec.INT.xmap(Ticks::duration, MinecraftTimeDuration::ticks)
    public val VEC2: Codec<Vec2> = Codec.FLOAT.listOf().comapFlatMap(
        { Util.fixedSize(it, 2).map { vec -> Vec2(vec[0], vec[1]) } },
        { vec -> listOf(vec.x, vec.y) }
    )

    public inline fun <reified E: Enum<E>> enum(
        mapper: (E) -> String = { it.name.lowercase() }
    ): Codec<E> {
        return enum(enumEntries<E>().associateBy(mapper))
    }

    public fun <E: Enum<E>> enum(constants: Map<String, E>): Codec<E> {
        val map = HashBiMap.create<String, E>(constants)
        val inverse = map.inverse()
        return Codec.STRING.xmap(map::get, inverse::get)
    }

    public inline fun <reified E: Enum<E>> optionalEnum(
        mapper: (E) -> String = { it.name.lowercase() }
    ): Codec<Optional<E>> {
        return optionalEnum(enumEntries<E>().associateBy(mapper).mapValues { Optional.of(it.value) })
    }

    public fun <E: Enum<E>> optionalEnum(constants: Map<String, Optional<E>>): Codec<Optional<E>> {
        val map = HashBiMap.create<Optional<String>, Optional<E>>(constants.size)
        var hasEmpty = false
        for ((key, value) in constants) {
            map[Optional.of(key)] = value
            if (value.isEmpty) {
                hasEmpty = true
            }
        }
        if (!hasEmpty) {
            map[Optional.empty()] = Optional.empty()
        }

        val inverse = map.inverse()
        return ExtraCodecs.optionalEmptyMap(Codec.STRING).xmap(map::get, inverse::get)
    }
}