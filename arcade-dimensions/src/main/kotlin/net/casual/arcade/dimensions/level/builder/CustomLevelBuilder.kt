package net.casual.arcade.dimensions.level.builder

import net.casual.arcade.dimensions.level.*
import net.casual.arcade.dimensions.level.LevelProperties.DifficultyProperties
import net.casual.arcade.dimensions.level.LevelProperties.WeatherProperties
import net.casual.arcade.dimensions.level.factory.CustomLevelFactoryConstructor
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.utils.ResourceUtils
import net.minecraft.core.Holder
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import org.apache.commons.lang3.mutable.MutableLong
import java.util.*
import kotlin.jvm.optionals.getOrNull

public class CustomLevelBuilder {
    private var properties: LevelProperties = LevelProperties()

    private var constructor = CustomLevelFactoryConstructor.DEFAULT

    private var key: ResourceKey<Level>? = null
    private var stem: LevelStem? = null
    private var stemKey: ResourceKey<LevelStem>? = null
    private var type: Holder<DimensionType>? = null
    private var generator: ChunkGenerator? = null

    public var seed: Long = 0
    public var flat: Boolean = false
    public var tickTime: Boolean = false
    public var generateStructures: Boolean = false
    public var debug: Boolean = false

    public var persistence: LevelPersistence = LevelPersistence.Temporary

    public var timeOfDay: Long
        set(value) { this.timeOfDay(value) }
        get() = throw UnsupportedOperationException()

    public var dimensionKey: ResourceKey<Level>
        set(value) { this.dimensionKey(value) }
        get() = throw UnsupportedOperationException()

    public var levelStem: LevelStem
        set(value) { this.levelStem(value) }
        get() = throw UnsupportedOperationException()

    public var dimensionType: Holder<DimensionType>
        set(value) { this.dimensionType(value) }
        get() = throw UnsupportedOperationException()

    public var chunkGenerator: ChunkGenerator
        set(value) { this.chunkGenerator(value) }
        get() = throw UnsupportedOperationException()

    public fun constructor(constructor: CustomLevelFactoryConstructor): CustomLevelBuilder {
        this.constructor = constructor
        return this
    }

    public fun dimensionKey(key: ResourceKey<Level>): CustomLevelBuilder {
        this.key = key
        return this
    }

    public fun dimensionKey(location: ResourceLocation): CustomLevelBuilder {
        this.key = ResourceKey.create(Registries.DIMENSION, location)
        return this
    }

    public fun randomDimensionKey(): CustomLevelBuilder {
        this.dimensionKey(ResourceUtils.random())
        return this
    }

    public fun timeOfDay(time: Long): CustomLevelBuilder {
        this.properties.dayTime = Optional.of(MutableLong(time))
        return this
    }

    public fun weather(weather: WeatherProperties): CustomLevelBuilder {
        this.properties.weather = Optional.of(weather)
        return this
    }

    public fun weather(builder: WeatherProperties.() -> Unit): CustomLevelBuilder {
        val weather = this.properties.weather.orElseGet(::WeatherProperties)
        weather.builder()
        return this
    }

    public fun difficulty(difficulty: DifficultyProperties): CustomLevelBuilder {
        this.properties.difficulty = Optional.of(difficulty)
        return this
    }

    public fun difficulty(builder: DifficultyProperties.() -> Unit): CustomLevelBuilder {
        val difficulty = this.properties.difficulty.orElseGet(::DifficultyProperties)
        difficulty.builder()
        return this
    }

    public fun gameRules(rules: GameRules): CustomLevelBuilder {
        this.properties.gameRules = Optional.of(rules)
        return this
    }

    public fun gameRules(builder: GameRules.() -> Unit): CustomLevelBuilder {
        val rules = this.properties.gameRules.orElseGet(::GameRules)
        rules.builder()
        return this
    }

    public fun levelStem(stem: LevelStem): CustomLevelBuilder {
        this.stem = stem
        return this
    }

    public fun levelStem(stem: ResourceKey<LevelStem>): CustomLevelBuilder {
        this.stemKey = stem
        return this
    }

    public fun dimensionType(type: Holder<DimensionType>): CustomLevelBuilder {
        this.type = type
        return this
    }

    public fun dimensionType(block: DimensionTypeBuilder.() -> Unit): CustomLevelBuilder {
        val builder = DimensionTypeBuilder()
        builder.block()
        this.type = Holder.direct(builder.build())
        return this
    }

    public fun chunkGenerator(generator: ChunkGenerator): CustomLevelBuilder {
        this.generator = generator
        return this
    }

    public fun seed(seed: Long): CustomLevelBuilder {
        this.seed = seed
        return this
    }

    public fun randomSeed(): CustomLevelBuilder {
        this.seed = WorldOptions.randomSeed()
        return this
    }

    public fun flat(flat: Boolean): CustomLevelBuilder {
        this.flat = flat
        return this
    }

    public fun tickTime(tickTime: Boolean): CustomLevelBuilder {
        this.tickTime = tickTime
        return this
    }

    public fun generateStructures(generateStructures: Boolean): CustomLevelBuilder {
        this.generateStructures = generateStructures
        return this
    }

    public fun debug(debug: Boolean): CustomLevelBuilder {
        this.debug = debug
        return this
    }

    public fun persistence(persistence: LevelPersistence): CustomLevelBuilder {
        this.persistence = persistence
        return this
    }

    public fun vanillaDefaults(dimension: VanillaDimension): CustomLevelBuilder {
        return this.levelStem(dimension.getStemKey()).tickTime(dimension.doesTimeTick())
    }

    public fun build(server: MinecraftServer): CustomLevel {
        val key = requireNotNull(this.key) { "Dimension key must be specified" }
        var stem = this.stem ?: server.registryAccess().registry(Registries.LEVEL_STEM)
            .flatMap { it.getOptional(this.stemKey) }.getOrNull()
        if (stem == null) {
            val dimensionType = this.type ?: server.overworld().dimensionTypeRegistration()
            val generator = this.generator ?: VoidChunkGenerator(server)
            stem = LevelStem(dimensionType, generator)
        }

        val options = LevelGenerationOptions(stem, this.seed, this.flat, this.tickTime, this.generateStructures, this.debug)
        return this.constructor.construct(this.properties, options, this.persistence).create(server, key)
    }
}