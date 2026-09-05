/*
 * Copyright (c) 2026 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.minigame.template.level

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevels
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevelsBuilder
import net.casual.arcade.minigame.Minigame
import net.casual.arcade.minigame.managers.MinigameLevelManager.LevelOwnership
import net.casual.arcade.utils.IdentifierUtils
import net.casual.arcade.utils.arcade
import net.casual.arcade.utils.serialization.codec.ArcadeExtraCodecs
import net.minecraft.core.registries.Registries
import net.minecraft.resources.Identifier
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.util.StringRepresentable
import net.minecraft.world.level.Level
import net.minecraft.world.level.levelgen.WorldOptions
import java.util.*
import kotlin.jvm.optionals.getOrNull

public data class VanillaLikeLevelsTemplate(
    public val dimensions: Map<VanillaDimension, LevelSettings> = mapOf(),
    public val ownership: LevelOwnership = LevelOwnership.Exclusive
) {
    public fun addTo(
        minigame: Minigame,
        id: (VanillaDimension) -> Identifier = { dimension -> dimension.arcadeId() },
        block: CustomLevelBuilder.(VanillaDimension) -> Unit = { }
    ): VanillaLikeLevels {
        val levels = this.generate(minigame.server, block)
        for (dimension in VanillaDimension.entries) {
            val level = levels.get(dimension) ?: continue
            minigame.levels.add(id(dimension), level, this.ownership)
        }
        return levels
    }

    public fun generate(
        server: MinecraftServer,
        block: CustomLevelBuilder.(VanillaDimension) -> Unit = { }
    ): VanillaLikeLevels {
        val shared = WorldOptions.randomSeed()
        val persistence = this.ownership.persistence()
        return VanillaLikeLevelsBuilder.build(server) {
            for (dimension in VanillaDimension.entries) {
                val settings = settingsFor(dimension)
                set(dimension) {
                    dimensionKey(settings.key ?: randomKeyFor(dimension))
                    seed(settings.seed ?: shared)
                    persistence(persistence)
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
        public val seed: Long? = null
    ) {
        public companion object {
            @JvmField
            public val CODEC: Codec<LevelSettings> = RecordCodecBuilder.create { instance ->
                instance.group(
                    ArcadeExtraCodecs.DIMENSION.optionalFieldOf("dimension").forGetter { settings -> Optional.ofNullable(settings.key) },
                    Codec.LONG.optionalFieldOf("seed").forGetter { settings -> Optional.ofNullable(settings.seed) }
                ).apply(instance) { key, seed -> LevelSettings(key.getOrNull(), seed.getOrNull()) }
            }
        }
    }

    public companion object {
        @JvmField
        public val OVERWORLD: Identifier = VanillaDimension.Overworld.arcadeId()

        @JvmField
        public val NETHER: Identifier = VanillaDimension.Nether.arcadeId()

        @JvmField
        public val END: Identifier = VanillaDimension.End.arcadeId()

        private val DIMENSIONS_CODEC: Codec<Map<VanillaDimension, LevelSettings>> = Codec.simpleMap(
            VanillaDimension.CODEC,
            LevelSettings.CODEC,
            StringRepresentable.keys(VanillaDimension.entries.toTypedArray())
        ).codec()

        @JvmField
        public val CODEC: Codec<VanillaLikeLevelsTemplate> = RecordCodecBuilder.create { instance ->
            instance.group(
                DIMENSIONS_CODEC.optionalFieldOf("dimensions", mapOf()).forGetter(VanillaLikeLevelsTemplate::dimensions),
                LevelOwnership.CODEC.optionalFieldOf("ownership", LevelOwnership.Exclusive).forGetter(VanillaLikeLevelsTemplate::ownership)
            ).apply(instance, ::VanillaLikeLevelsTemplate)
        }

        private fun randomKeyFor(dimension: VanillaDimension): ResourceKey<Level> {
            val path = dimension.getDimensionKey().identifier().path
            return ResourceKey.create(Registries.DIMENSION, IdentifierUtils.random { "${path}_$it" })
        }

        private fun VanillaDimension.arcadeId(): Identifier {
            return arcade(this.serializedName)
        }
    }
}
