package net.casual.arcade.dimensions.level.builder

import net.casual.arcade.dimensions.ArcadeDimensions
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelGenerationOptions
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.LevelProperties
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
import net.minecraft.world.flag.FeatureFlagSet
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.chunk.ChunkGenerator
import net.minecraft.world.level.dimension.DimensionType
import net.minecraft.world.level.dimension.LevelStem
import net.minecraft.world.level.levelgen.WorldOptions
import org.apache.commons.lang3.mutable.MutableLong
import org.jetbrains.annotations.ApiStatus.Experimental
import java.util.*

/**
 * A builder class to help construct
 * instances of [CustomLevel].
 *
 * Here's an example use case:
 * ```
 * val level = CustomLevelBuilder.build(server) {
 *     // Set our dimension key
 *     dimensionKey = ResourceKey.create(
 *         Registries.DIMENSION,
 *         ResourceLocation.withDefaultNamespace("foo")
 *     )
 *     // Use the vanilla dimension type and chunk generator
 *     levelStem(LevelStem.OVERWORLD)
 *
 *     // Other properties
 *     tickTime = true
 *     generateStructures = true
 *     persistence = LevelPersistence.Temporary
 *     randomSeed()
 * }
 * ```
 *
 * @see CustomLevel
 */
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

    /**
     * The world seed.
     */
    public var seed: Long = 0

    /**
     * Whether the world is considered to be flat.
     *
     * This doesn't change whether the world is actually
     * *flat* or not. It just effects how the world
     * is rendered on the client.
     *
     * Flat worlds have their sky rendered lower,
     * so the dark sky below sea level doesn't render,
     * and also changes the color of the fog.
     */
    public var flat: Boolean = false

    /**
     * Whether the world should tick time.
     *
     * This refers to the day-light cycle time.
     */
    public var tickTime: Boolean = false

    /**
     * Whether the world should naturally generate structures.
     */
    public var generateStructures: Boolean = false

    /**
     * Whether the world is the [debug world](https://minecraft.wiki/w/Debug_mode).
     */
    public var debug: Boolean = false

    /**
     * Determines whether the world persists when the server stops.
     *
     * @see LevelPersistence
     */
    public var persistence: LevelPersistence = LevelPersistence.Temporary

    /**
     * Sets the initial time of day.
     */
    public var timeOfDay: Long
        set(value) { this.timeOfDay(value) }
        get() = throw UnsupportedOperationException()

    /**
     * Sets the dimension key.
     */
    public var dimensionKey: ResourceKey<Level>
        set(value) { this.dimensionKey(value) }
        get() = throw UnsupportedOperationException()

    /**
     * Sets the [LevelStem] which determines the [dimensionType]
     * and [chunkGenerator].
     *
     * This doesn't need to be specified if both the [dimensionType]
     * and [chunkGenerator] are specified.
     */
    public var levelStem: Holder<LevelStem>
        set(value) { this.levelStem(value) }
        get() = throw UnsupportedOperationException()

    /**
     * Sets the dimension type.
     *
     * If [levelStem] is specified this doesn't need to be.
     */
    public var dimensionType: Holder<DimensionType>
        set(value) { this.dimensionType(value) }
        get() = throw UnsupportedOperationException()

    /**
     * Sets the chunk generator.
     *
     * If [levelStem] is specified this doesn't need to be.
     */
    public var chunkGenerator: ChunkGenerator
        set(value) { this.chunkGenerator(value) }
        get() = throw UnsupportedOperationException()

    /**
     * This sets the [CustomLevel] factory constructor.
     *
     * You only need to modify this if you want to construct your
     * own implementation of [CustomLevel].
     * In which case see [CustomLevelFactoryConstructor].
     *
     * @param constructor The factory constructor.
     * @return This builder.
     */
    public fun constructor(constructor: CustomLevelFactoryConstructor): CustomLevelBuilder {
        this.constructor = constructor
        return this
    }

    /**
     * Sets the dimension key.
     *
     * @param key The dimension key.
     * @return This builder.
     */
    public fun dimensionKey(key: ResourceKey<Level>): CustomLevelBuilder {
        this.key = key
        return this
    }

    /**
     * Sets the dimension key.
     *
     * @param location The dimension key location.
     * @return This builder.
     */
    public fun dimensionKey(location: ResourceLocation): CustomLevelBuilder {
        this.key = ResourceKey.create(Registries.DIMENSION, location)
        return this
    }

    /**
     * Sets the dimension key to a random key.
     *
     * This is useful for runtime generated dimensions where
     * the dimension key doesn't matter.
     *
     * @return This builder.
     */
    public fun randomDimensionKey(): CustomLevelBuilder {
        this.dimensionKey(ResourceUtils.random())
        return this
    }

    /**
     * Sets the level properties to their default values.
     *
     * This prevents the [CustomLevel] from inheriting these
     * properties from the primary level.
     *
     * @return This builder.
     */
    public fun defaultLevelProperties(): CustomLevelBuilder {
        return this.timeOfDay(0)
            .weather(WeatherProperties())
            .difficulty(DifficultyProperties())
            .gameRules(GameRules(FeatureFlagSet.of()))
    }

    /**
     * Sets the initial time of day.
     *
     * @param time The time of day (ticks).
     * @return This builder.
     */
    public fun timeOfDay(time: Long): CustomLevelBuilder {
        this.properties.dayTime = Optional.of(MutableLong(time))
        return this
    }

    /**
     * Sets the initial weather properties.
     *
     * @param weather The weather properties.
     * @return This builder.
     */
    public fun weather(weather: WeatherProperties): CustomLevelBuilder {
        this.properties.weather = Optional.of(weather)
        return this
    }

    /**
     * Modifies the weather properties.
     *
     * @param modifier The method to modify the weather properties.
     * @return This builder.
     */
    public fun weather(modifier: WeatherProperties.() -> Unit): CustomLevelBuilder {
        val weather = this.properties.weather.orElseGet(::WeatherProperties)
        weather.modifier()
        return this
    }

    /**
     * Sets the difficulty properties.
     *
     * @param difficulty The difficulty properties.
     * @return This builder.
     */
    public fun difficulty(difficulty: DifficultyProperties): CustomLevelBuilder {
        this.properties.difficulty = Optional.of(difficulty)
        return this
    }

    /**
     * Modifies the difficulty properties.
     *
     * @param builder The method to modify the difficulty properties.
     * @return This builder.
     */
    public fun difficulty(builder: DifficultyProperties.() -> Unit): CustomLevelBuilder {
        val difficulty = this.properties.difficulty.orElseGet(::DifficultyProperties)
        difficulty.builder()
        return this
    }

    /**
     * Sets the game rules.
     *
     * @param rules The game rules.
     * @return This builder.
     */
    public fun gameRules(rules: GameRules): CustomLevelBuilder {
        this.properties.gameRules = Optional.of(rules)
        return this
    }

    /**
     * Modifies the game rules.
     *
     * @param builder The method to modify the game rules.
     * @return This builder.
     */
    public fun gameRules(builder: GameRules.() -> Unit): CustomLevelBuilder {
        val rules = this.properties.gameRules.orElseGet { GameRules(FeatureFlagSet.of()) }
        rules.builder()
        return this
    }

    /**
     * Sets the level stem.
     *
     * This doesn't need to be specified if both the [dimensionType]
     * and [chunkGenerator] are specified.
     *
     * @param stem The level stem.
     * @return This builder.
     */
    public fun levelStem(stem: Holder<LevelStem>): CustomLevelBuilder {
        this.stem = stem
        return this
    }

    /**
     * Sets the level stem.
     *
     * This doesn't need to be specified if both the [dimensionType]
     * and [chunkGenerator] are specified.
     *
     * @param stem The level stem key.
     * @return This builder.
     */
    public fun levelStem(stem: ResourceKey<LevelStem>): CustomLevelBuilder {
        this.stemKey = stem
        return this
    }

    /**
     * Sets the dimension type.
     *
     * If [levelStem] is specified this doesn't need to be.
     *
     * @param type The dimension type.
     * @return This builder.
     */
    public fun dimensionType(type: Holder<DimensionType>): CustomLevelBuilder {
        this.type = type
        return this
    }

    /**
     * Sets the dimension type.
     *
     * If [levelStem] is specified this doesn't need to be.
     *
     * @param key The dimension type key.
     * @return This builder.
     */
    public fun dimensionType(key: ResourceKey<DimensionType>): CustomLevelBuilder {
        this.typeKey = key
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
     * You can register your dimension type in
     * `/resources/data/<namespace>/dimension_type/<dimension_type>.json`
     * or you can register it programmatically:
     *
     * ```
     * override fun onInitialize() {
     *     val dimensionTypeKey = ResourceKey.create(
     *         Registries.DIMENSION_TYPE,
     *         ResourceLocation.withDefaultNamespace("foo")
     *     )
     *
     *     RegistryEventHandler.register(Registries.DIMENSION_TYPE) { (registry) ->
     *         Registry.register(registry, dimensionTypeKey, DimensionTypeBuilder.build {
     *             bedWorks = false
     *             piglinSafe = true
     *             height = 512
     *             // ...
     *         })
     *     }
     * }
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


    /**
     * Sets the chunk generator.
     *
     * If [levelStem] is specified this doesn't need to be.
     *
     * @param generator The chunk generator.
     * @return This builder.
     */
    public fun chunkGenerator(generator: ChunkGenerator): CustomLevelBuilder {
        this.generator = generator
        return this
    }


    /**
     * Sets the world seed.
     *
     * @param seed The world seed.
     * @return This builder.
     */
    public fun seed(seed: Long): CustomLevelBuilder {
        this.seed = seed
        return this
    }

    /**
     * Sets the world seed to a random seed.
     *
     * @return This builder.
     */
    public fun randomSeed(): CustomLevelBuilder {
        this.seed = WorldOptions.randomSeed()
        return this
    }


    /**
     * Sets whether the world is considered to be flat.
     *
     * This doesn't change whether the world is actually
     * *flat* or not. It just effects how the world
     * is rendered on the client.
     *
     * Flat worlds have their sky rendered lower,
     * so the dark sky below sea level doesn't render,
     * and also changes the color of the fog.
     *
     * @param flat Whether the world is considered to be flat.
     * @return This builder.
     */
    public fun flat(flat: Boolean): CustomLevelBuilder {
        this.flat = flat
        return this
    }


    /**
     * Sets whether the world should tick time.
     *
     * This refers to the day-light cycle time.
     *
     * @param tickTime Whether the world should tick time.
     * @return This builder.
     */
    public fun tickTime(tickTime: Boolean): CustomLevelBuilder {
        this.tickTime = tickTime
        return this
    }


    /**
     * Sets whether the world should naturally generate structures.
     *
     * @param generateStructures Whether the world should naturally generate structures.
     * @return This builder.
     */
    public fun generateStructures(generateStructures: Boolean): CustomLevelBuilder {
        this.generateStructures = generateStructures
        return this
    }

    /**
     * Sets whether the world is the [debug world](https://minecraft.wiki/w/Debug_mode).
     *
     * @param debug Whether the world is the debug world.
     * @return This builder.
     */
    public fun debug(debug: Boolean): CustomLevelBuilder {
        this.debug = debug
        return this
    }

    /**
     * Sets the custom spawner factories for the world.
     * This will replace any existing spawners.
     *
     * This allows you to declare custom entity spawning rules
     * for the world.
     *
     * @param spawners The custom spawner factories.
     * @return This builder.
     * @see CustomSpawnerFactory
     */
    public fun customSpawners(vararg spawners: CustomSpawnerFactory): CustomLevelBuilder {
        this.spawners.clear()
        this.spawners.addAll(spawners)
        return this
    }

    /**
     * Sets the custom spawner factories for the world.
     * This will replace any existing spawners.
     *
     * This allows you to declare custom entity spawning rules
     * for the world.
     *
     * @param spawners The custom spawner factories.
     * @return This builder.
     * @see CustomSpawnerFactory
     */
    public fun customSpawners(spawners: List<CustomSpawnerFactory>): CustomLevelBuilder {
        this.spawners.clear()
        this.spawners.addAll(spawners)
        return this
    }

    /**
     * Adds custom spawner factories to the world.
     *
     * This allows you to declare custom entity spawning rules
     * for the world.
     *
     * @param spawners The custom spawner factories.
     * @return This builder.
     * @see CustomSpawnerFactory
     */
    public fun addCustomSpawners(vararg spawners: CustomSpawnerFactory): CustomLevelBuilder {
        this.spawners.addAll(spawners)
        return this
    }

    /**
     * Adds custom spawner factories to the world.
     *
     * This allows you to declare custom entity spawning rules
     * for the world.
     *
     * @param spawners The custom spawner factories.
     * @return This builder.
     * @see CustomSpawnerFactory
     */
    public fun addCustomSpawners(spawners: Collection<CustomSpawnerFactory>): CustomLevelBuilder {
        this.spawners.addAll(spawners)
        return this
    }

    /**
     * Sets the persistence of the world.
     *
     * @param persistence The persistence of the world.
     * @return This builder.
     */
    public fun persistence(persistence: LevelPersistence): CustomLevelBuilder {
        this.persistence = persistence
        return this
    }

    /**
     * Configures this builder based on the given vanilla dimension.
     *
     * @param dimension The vanilla dimension.
     * @return This builder.
     */
    public fun vanillaDefaults(dimension: VanillaDimension): CustomLevelBuilder {
        return this.levelStem(dimension.getStemKey())
            .tickTime(dimension.doesTimeTick())
            .addCustomSpawners(dimension.getCustomSpawners())
            .generateStructures(true)
    }

    /**
     * Builds the [CustomLevel] instance.
     *
     * This **does not** add the level to the server.
     * If you want to add the level to the server, you
     * probably want to call [ArcadeDimensions.add].
     *
     * @param server The server.
     * @return The custom level.
     */
    public fun build(server: MinecraftServer): CustomLevel {
        val key = requireNotNull(this.key) { "Dimension key must be specified" }
        var stem = this.stem ?: Optional.ofNullable(this.stemKey).flatMap { stemKey ->
            server.registryAccess().lookup(Registries.LEVEL_STEM).flatMap { it.get(stemKey) }
        }.orElse(null)
        if (stem == null) {
            val dimensionType = this.type ?: Optional.ofNullable(this.typeKey).flatMap { typeKey ->
                server.registryAccess().lookup(Registries.DIMENSION_TYPE).flatMap { it.get(typeKey) }
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
        /**
         * Builds a [CustomLevel] instance.
         *
         * This **does not** add the level to the server.
         * If you want to add the level to the server, you
         * probably want to call [ArcadeDimensions.add].
         *
         * @param server The server.
         * @param block The method to configure the builder.
         * @return The custom level.
         */
        public fun build(server: MinecraftServer, block: CustomLevelBuilder.() -> Unit): CustomLevel {
            val builder = CustomLevelBuilder()
            builder.block()
            return builder.build(server)
        }
    }
}