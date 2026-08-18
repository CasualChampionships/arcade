/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level.vanilla

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.utils.EnumUtils
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.WorldOptions
import java.util.*
import kotlin.jvm.optionals.getOrNull

public data class VanillaLikeLevelsSettings(
    public val dimensions: Map<VanillaDimension, LevelSettings> = mapOf()
) {
    public fun loadOrGenerate(
        server: MinecraftServer,
        block: CustomLevelBuilder.(VanillaDimension) -> Unit = { }
    ): VanillaLikeLevels {
        val keys = EnumUtils.mapOf<VanillaDimension, ResourceKey<Level>>()
        for (dimension in VanillaDimension.entries) {
            keys[dimension] = this.settingsFor(dimension).key ?: randomKeyFor(dimension)
        }
        return this.load(server, keys) ?: this.generate(server, keys, block)
    }

    private fun load(
        server: MinecraftServer,
        keys: Map<VanillaDimension, ResourceKey<Level>>
    ): VanillaLikeLevels? {
        val levels = EnumUtils.mapOf<VanillaDimension, CustomLevel>()
        for ((dimension, key) in keys) {
            val level = server.getLevel(key) ?: CustomLevel.read(server, key)
            if (level !is CustomLevel) {
                return null
            }
            levels[dimension] = level
        }
        return VanillaLikeLevels(levels)
    }

    private fun generate(
        server: MinecraftServer,
        keys: Map<VanillaDimension, ResourceKey<Level>>,
        block: CustomLevelBuilder.(VanillaDimension) -> Unit
    ): VanillaLikeLevels {
        val shared = WorldOptions.randomSeed()
        return VanillaLikeLevelsBuilder.build(server) {
            for ((dimension, key) in keys) {
                val settings = settingsFor(dimension)
                set(dimension) {
                    dimensionKey(key)
                    seed(settings.seed ?: shared)
                    persistence(settings.persistence)
                    defaultLevelProperties()
                    block(dimension)
                }
            }
        }
    }

    private fun settingsFor(dimension: VanillaDimension): LevelSettings {
        return this.dimensions[dimension] ?: LevelSettings()
    }

    public data class LevelSettings(
        public val key: ResourceKey<Level>? = null,
        public val persistence: LevelPersistence = LevelPersistence.Temporary,
        public val seed: Long? = null
    ) {
        public companion object {
            @JvmField
            public val CODEC: Codec<LevelSettings> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ArcadeExtraCodecs.DIMENSION.optionalFieldOf("dimension").forGetter { settings -> Optional.ofNullable(settings.key) },
                    LevelPersistence.CODEC.optionalFieldOf("persistence", LevelPersistence.Temporary).forGetter(LevelSettings::persistence),
                    Codec.LONG.optionalFieldOf("seed").forGetter { settings -> Optional.ofNullable(settings.seed) }
                ).apply(instance) { key, persistence, seed -> LevelSettings(key.getOrNull(), persistence, seed.getOrNull()) }
            }
        }
    }

    public companion object {
        @JvmField
        public val CODEC: Codec<VanillaLikeLevelsSettings> = Codec.simpleMap(
            VanillaDimension.CODEC,
            LevelSettings.CODEC,
            StringRepresentable.keys(VanillaDimension.entries.toTypedArray())
        ).xmap(::VanillaLikeLevelsSettings, VanillaLikeLevelsSettings::dimensions).codec()

        @JvmStatic
        public fun of(levels: VanillaLikeLevels): VanillaLikeLevelsSettings {
            val settings = EnumUtils.mapOf<VanillaDimension, LevelSettings>()
            for (dimension in VanillaDimension.entries) {
                val level = levels.get(dimension) ?: continue
                settings[dimension] = LevelSettings(level.dimension(), level.persistence, level.options.seed)
            }
            return VanillaLikeLevelsSettings(settings)
        }

        private fun randomKeyFor(dimension: VanillaDimension): ResourceKey<Level> {
            val path = dimension.getDimensionKey().identifier().path
            return ResourceKey.create(Registries.DIMENSION, IdentifierUtils.random { "${path}_$it" })
        }
    }
}
