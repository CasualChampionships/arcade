package net.casual.arcade.dimensions.level.builder

import net.casual.arcade.dimensions.level.*
import net.casual.arcade.dimensions.level.LevelProperties.DifficultyProperties
import net.casual.arcade.dimensions.level.LevelProperties.WeatherProperties
import net.casual.arcade.dimensions.level.factory.CustomLevelFactoryConstructor
import net.casual.arcade.dimensions.level.spawner.CustomSpawnerFactory
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
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
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.*
import kotlin.collections.ArrayList
import kotlin.jvm.optionals.getOrNull

public class CustomLevelBuilder {
    private val properties = LevelProperties()
    private val spawners = ArrayList<CustomSpawnerFactory>()

    private var constructor = CustomLevelFactoryConstructor.DEFAULT

    private var key: ResourceKey<Level>? = null
    private var stem: Holder<LevelStem>? = null
    private var stemKey: ResourceKey<LevelStem>? = null
    private var type: Holder<DimensionType>? = null
    private var typeKey: ResourceKey<DimensionType>? = null
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

    public var levelStem: Holder<LevelStem>
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

    public fun levelStem(stem: Holder<LevelStem>): CustomLevelBuilder {
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

    public fun dimensionType(type: ResourceKey<DimensionType>): CustomLevelBuilder {
        this.typeKey = type
        return this
    }

    /**
     * This is a quick and dirty way to add custom dimension
     * types without registering them.
     *
     * However, because these are not registered, they do
     * not sync properly with the client; instead, a default
     * dimension type is sent to the client.
     * Everything will work properly on the server, but the
     * client may de-sync under certain circumstances.
     *
     * ```
     *
     * ```
     *
     * @param block The method to build your dimension type.
     * @see dimensionType
     */
    @Experimental
    public fun dimensionType(block: DimensionTypeBuilder.() -> Unit): CustomLevelBuilder {
        val type = DimensionTypeBuilder.build(block)
        return this.dimensionType(Holder.direct(type))
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

    public fun customSpawners(vararg spawners: CustomSpawnerFactory): CustomLevelBuilder {
        this.spawners.clear()
        this.spawners.addAll(spawners)
        return this
    }

    public fun customSpawners(spawners: List<CustomSpawnerFactory>): CustomLevelBuilder {
        this.spawners.clear()
        this.spawners.addAll(spawners)
        return this
    }

    public fun addCustomSpawners(vararg spawners: CustomSpawnerFactory): CustomLevelBuilder {
        this.spawners.addAll(spawners)
        return this
    }

    public fun addCustomSpawners(spawners: Collection<CustomSpawnerFactory>): CustomLevelBuilder {
        this.spawners.addAll(spawners)
        return this
    }

    public fun persistence(persistence: LevelPersistence): CustomLevelBuilder {
        this.persistence = persistence
        return this
    }

    public fun vanillaDefaults(dimension: VanillaDimension): CustomLevelBuilder {
        return this.levelStem(dimension.getStemKey())
            .tickTime(dimension.doesTimeTick())
            .addCustomSpawners(dimension.getCustomSpawners())
            .generateStructures(true)
    }

    public fun build(server: MinecraftServer): CustomLevel {
        val key = requireNotNull(this.key) { "Dimension key must be specified" }
        var stem = this.stem ?: Optional.ofNullable(this.stemKey).flatMap { stemKey ->
            server.registryAccess().registry(Registries.LEVEL_STEM).flatMap { it.getHolder(stemKey) }
        }.orElse(null)
        if (stem == null) {
            val dimensionType = this.type ?: Optional.ofNullable(this.typeKey).flatMap { typeKey ->
                server.registryAccess().registry(Registries.DIMENSION_TYPE).flatMap { it.getHolder(typeKey) }
            }.orElseThrow { IllegalArgumentException("Unknown dimension type specified") }

            val generator = this.generator ?: throw IllegalArgumentException("Chunk generator must be specified")
            stem = Holder.direct(LevelStem(dimensionType, generator))
        }

        val options = LevelGenerationOptions(
            stem, this.seed, this.flat, this.tickTime, this.generateStructures, this.debug, this.spawners
        )
        return this.constructor.construct(this.properties, options, this.persistence).create(server, key)
    }

    public companion object {
        public fun build(server: MinecraftServer, block: CustomLevelBuilder.() -> Unit): CustomLevel {
            val builder = CustomLevelBuilder()
            builder.block()
            return builder.build(server)
        }
    }
}