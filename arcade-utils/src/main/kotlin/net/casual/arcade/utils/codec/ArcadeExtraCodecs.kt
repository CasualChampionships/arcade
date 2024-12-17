package net.casual.arcade.utils.codec

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.Dynamic
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.Util
import net.minecraft.core.registries.Registries
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.mutable.MutableInt
import org.apache.commons.lang3.mutable.MutableLong
import java.nio.file.Path
import java.util.*
import kotlin.enums.enumEntries
import kotlin.io.path.pathString

public object ArcadeExtraCodecs {
    public val MUTABLE_INT: Codec<MutableInt> = Codec.INT.xmap(::MutableInt, MutableInt::getValue)
    public val MUTABLE_LONG: Codec<MutableLong> = Codec.LONG.xmap(::MutableLong, MutableLong::getValue)
    public val PATH: Codec<Path> = Codec.STRING.xmap(Path::of, Path::pathString)
    public val VEC2: Codec<Vec2> = Codec.FLOAT.listOf().comapFlatMap(
        { Util.fixedSize(it, 2).map { vec -> Vec2(vec[0], vec[1]) } },
        { vec -> listOf(vec.x, vec.y) }
    )
    public val AABB: Codec<AABB> = RecordCodecBuilder.create { instance ->
        instance.group(
            Vec3.CODEC.fieldOf("from").forGetter { it.minPosition },
            Vec3.CODEC.fieldOf("to").forGetter { it.maxPosition }
        ).apply(instance, ::AABB)
    }
    public val JSON_OBJECT: Codec<JsonObject> = ExtraCodecs.JSON.comapFlatMap(
        { json -> if (json !is JsonObject) DataResult.error { "Input wasn't JsonObject" } else DataResult.success(json) },
        { json -> json }
    )
    public val GAMERULES: Codec<GameRules> = Codec.PASSTHROUGH.xmap(
        { GameRules(FeatureFlagSet.of(), it) },
        { Dynamic(NbtOps.INSTANCE, it.createTag()) }
    )
    public val DIMENSION: Codec<ResourceKey<Level>> = ResourceKey.codec(Registries.DIMENSION)

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

    public fun <K, V> keyedUnboundedMapCodec(
        keyCodec: Codec<K>,
        valueMapCodec: MapCodec<V>,
        keyName: String = "id"
    ): Codec<Map<K, V>>? {
        val entryCodec = RecordCodecBuilder.create<Pair<K, V>> { instance ->
            instance.group(
                keyCodec.fieldOf(keyName).forGetter { it.first },
                valueMapCodec.forGetter { it.second }
            ).apply(instance, ::Pair)
        }
        return entryCodec.listOf().xmap(
            { entries -> entries.toMap() },
            { map -> map.map { it.key to it.value } }
        )
    }
}