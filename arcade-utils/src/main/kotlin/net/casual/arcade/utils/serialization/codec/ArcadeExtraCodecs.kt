/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.utils.serialization.codec

import com.google.common.collect.HashBiMap
import com.google.gson.JsonObject
import com.mojang.datafixers.util.Either
import com.mojang.serialization.Codec
import com.mojang.serialization.DataResult
import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.util.ducks.GameRulesData
import net.casual.arcade.util.mixins.codec.FeatureFlagRegistryAccessor
import net.casual.arcade.util.mixins.codec.FeatureFlagSetAccessor
import net.casual.arcade.util.mixins.codec.FeatureFlagSetInvoker
import net.casual.arcade.utils.TimeUtils
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.util.ExtraCodecs
import net.minecraft.util.Util
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.flag.FeatureFlags
import net.minecraft.world.level.Level
import net.minecraft.world.level.gamerules.GameRuleMap
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec2
import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.mutable.MutableInt
import org.apache.commons.lang3.mutable.MutableLong
import java.nio.file.Path
import java.util.*
import java.util.function.Function
import kotlin.enums.enumEntries
import kotlin.io.path.pathString
import kotlin.time.Duration

public object ArcadeExtraCodecs {
    public val MUTABLE_INT: Codec<MutableInt> = Codec.INT.xmap(::MutableInt, MutableInt::toInt)
    public val MUTABLE_LONG: Codec<MutableLong> = Codec.LONG.xmap(::MutableLong, MutableLong::toLong)
    public val INT_RANGE: Codec<IntRange> = Codec.INT.listOf().comapFlatMap(
        { Util.fixedSize(it, 2).map { range -> IntRange(range[0], range[1]) } },
        { range -> listOf(range.first, range.last) }
    )
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
    private val GAMERULES_WITH_FLAGS = RecordCodecBuilder.create { instance ->
        instance.group(
            GameRuleMap.CODEC.fieldOf("rules").forGetter { rules -> (rules as GameRulesData).arcade_getGameRuleMap() },
            Codec.LONG.fieldOf("flags").forGetter { rules ->
                val flags = (rules as GameRulesData).arcade_getFeatureFlagSet()
                @Suppress("CAST_NEVER_SUCCEEDS")
                (flags as FeatureFlagSetAccessor).arcade_getMask()
            }
        ).apply(instance) { rules: GameRuleMap, flags: Long ->
            val universe = (FeatureFlags.REGISTRY as FeatureFlagRegistryAccessor).arcade_getUniverse()
            GameRules(FeatureFlagSetInvoker.arcade_init(universe, flags), rules)
        }
    }
    private val GAMERULES_WITHOUT_FLAGS = GameRules.codec(FeatureFlagSet.of())
    public val GAMERULES: Codec<GameRules> = Codec.withAlternative(GAMERULES_WITH_FLAGS, GAMERULES_WITHOUT_FLAGS)
    public val DIMENSION: Codec<ResourceKey<Level>> = ResourceKey.codec(Registries.DIMENSION)
    public val DURATION: Codec<Duration> = Codec.STRING.comapFlatMap(TimeUtils::parseToDuration, Duration::toString)

    public fun <T> mapWithAlternative(primary: MapCodec<T>, alternative: MapCodec<T>): MapCodec<T> {
        return Codec.mapEither(primary, alternative).xmap(
            { either -> Either.unwrap(either) },
            { value -> Either.left(value) }
        )
    }

    @JvmStatic
    public fun <T, S> Codec<T>.extend(
        builder: RecordCodecBuilder<T, S>,
        applier: (T, S) -> T
    ): Codec<T> {
        val extendable = if (this is MapCodec.MapCodecCodec) this.codec else MapCodec.assumeMapUnsafe(this)
        return extendable.extend(builder, applier).codec()
    }

    @JvmStatic
    public fun <T, S> MapCodec<T>.extend(
        builder: RecordCodecBuilder<T, S>,
        applier: (T, S) -> T
    ): MapCodec<T> {
        return RecordCodecBuilder.mapCodec { instance ->
            instance.group(
                this.forGetter(Function.identity()), builder
            ).apply(instance, applier)
        }
    }

    public inline fun <reified E: Enum<E>> enum(
        mapper: (E) -> String = { it.name.lowercase() }
    ): Codec<E> {
        return enum(enumEntries<E>().associateBy(mapper))
    }

    public fun <E: Enum<E>> enum(constants: Map<String, E>): Codec<E> {
        val map = HashBiMap.create(constants)
        return Codec.STRING.map(map)
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

        return ExtraCodecs.optionalEmptyMap(Codec.STRING).map(map)
    }

    @Deprecated(
        "Use keyedUnboundedMergedMap instead",
        ReplaceWith("this.keyedUnboundedMergedMap(keyCodec, valueMapCodec, keyName)")
    )
    public fun <K, V> keyedUnboundedMapCodec(
        keyCodec: Codec<K>,
        valueMapCodec: MapCodec<V>,
        keyName: String = "id"
    ): Codec<Map<K, V>> {
        return keyedUnboundedMergedMap(keyCodec, valueMapCodec, keyName)
    }

    public fun <K, V> keyedUnboundedMergedMap(
        keyCodec: Codec<K>,
        valueMapCodec: MapCodec<V>,
        keyName: String = "id"
    ): Codec<Map<K, V>> {
        return unboundedMergedMap(keyCodec.fieldOf(keyName), valueMapCodec)
    }

    public fun <K, V> unboundedMergedMap(
        keyCodec: MapCodec<K>,
        valueMapCodec: MapCodec<V>
    ): Codec<Map<K, V>> {
        val entryCodec = RecordCodecBuilder.create<Pair<K, V>> { instance ->
            instance.group(
                keyCodec.forGetter { it.first },
                valueMapCodec.forGetter { it.second }
            ).apply(instance, ::Pair)
        }
        return entryCodec.listOf().xmap(
            { entries -> entries.toMap() },
            { map -> map.map { it.key to it.value } }
        )
    }
}