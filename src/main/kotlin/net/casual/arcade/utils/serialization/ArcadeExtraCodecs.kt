package net.casual.arcade.utils.serialization

import com.google.common.collect.HashBiMap
import com.mojang.serialization.Codec
import net.casual.arcade.scheduler.MinecraftTimeDuration
import net.casual.arcade.scheduler.MinecraftTimeUnit.Ticks
import net.minecraft.Util
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import java.nio.file.Path
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
        return enum(E::class.java.enumConstants.associateBy(mapper))
    }

    public fun <E: Enum<E>> enum(constants: Map<String, E>): Codec<E> {
        val map = HashBiMap.create<String, E>(constants)
        val inverse = map.inverse()
        return Codec.STRING.xmap(map::get, inverse::get)
    }
}