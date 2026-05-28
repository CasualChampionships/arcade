/*
 * Copyright (c) 2024 senseiwells
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */
package net.casual.arcade.dimensions.level

import net.casual.arcade.dimensions.ArcadeDimensions
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.extensions.LevelClockExtension.Companion.clockExtension
import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.dimensions.level.factory.SimpleCustomLevelFactory
import net.casual.arcade.dimensions.mixins.level.MinecraftServerAccessor
import net.casual.arcade.dimensions.mixins.level.ServerLevelAccessor
import net.casual.arcade.dimensions.utils.LevelPersistenceTracker
import net.casual.arcade.dimensions.utils.getDimensionDataPath
import net.casual.arcade.dimensions.utils.impl.DerivedLevelData
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.level.server
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtUtils
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ProgressListener
import net.minecraft.util.datafix.DataFixTypes
import net.minecraft.world.Difficulty
import net.minecraft.world.level.Level
import net.minecraft.world.level.TicketStorage
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.gamerules.GameRules
import net.minecraft.world.level.saveddata.WeatherData
import net.minecraft.world.level.storage.LevelData
import net.minecraft.world.level.storage.TagValueInput
import net.minecraft.world.level.storage.TagValueOutput
import org.jetbrains.annotations.ApiStatus.OverrideOnly
import java.io.IOException
import java.nio.file.Path
import java.util.*
import java.util.concurrent.Executor
import kotlin.io.path.createParentDirectories
import kotlin.jvm.optionals.getOrElse
import kotlin.jvm.optionals.getOrNull

/**
 * Custom [ServerLevel] implementation allowing for
 * runtime level creation as well as full support for
 * serializing and deserializing.
 *
 * You should avoid constructing this class, instead
 * a [CustomLevelBuilder] is provided to help
 * create instances of this class.
 *
 * Given a [CustomLevel] is permanent then [properties],
 * [options], [persistence], and [factory] are written to
 * disk. This allows for an accurate re-creation of this
 * exact world when the server restarts.
 *
 * @param server The [MinecraftServer] instance.
 * @param key The dimension key.
 * @param properties The level properties.
 * @param options The level generation options.
 * @param persistence The persistence of the level.
 * @param factory The factory which is able to construct this instance.
 * @param dispatcher The background executor.
 * @see CustomLevelBuilder
 */
public open class CustomLevel(
    server: MinecraftServer,
    key: ResourceKey<Level>,
    public val properties: LevelProperties,
    public val options: LevelGenerationOptions,
    public val persistence: LevelPersistence = LevelPersistence.Temporary,
    private val factory: CustomLevelFactory = SimpleCustomLevelFactory(properties, options, persistence),
    dispatcher: Executor = (server as MinecraftServerAccessor).arcade_getExecutor(),
): ServerLevel(
    server,
    dispatcher,
    (server as MinecraftServerAccessor).arcade_getStorageSource(),
    DerivedLevelData(properties, options, server.worldData, server.worldData.overworldData()),
    key,
    options.stem.value(),
    options.debug,
    BiomeManager.obfuscateSeed(options.seed),
    ArrayList(),
    false
) {
    private val derivedLevelData: DerivedLevelData
        get() = this.levelData as DerivedLevelData

    init {
        // In case of server crash, we should still delete temporary levels
        if (!this.persistence.shouldSave()) {
            LevelPersistenceTracker.markAsTemporary(this.server(), this.dimension())
        }
    }

    /**
     * This method is called whenever the level is
     * added to the [MinecraftServer] instance.
     */
    @OverrideOnly
    public open fun onLoad() {
        this.loadCustomSpawners()
        this.loadForcedChunks()
        this.setSpawnSettings(
            this.levelData.difficulty != Difficulty.PEACEFUL && this.gameRules.get(GameRules.SPAWN_MONSTERS)
        )

        val initialClock = this.options.clock.getOrNull()
        if (initialClock != null && !this.clockExtension.initialized()) {
            this.clockExtension.set(initialClock)
        }
    }

    /**
     * This method is called whenever the level
     * is removed from the [MinecraftServer] instance.
     *
     * This will happen *before* any remaining players are
     * kicked from the level and before the level is saved.
     */
    @OverrideOnly
    public open fun onUnload() {

    }

    // We cannot reference `this.options`, as these methods
    // may be called during the super constructor

    override fun isFlat(): Boolean {
        return this.derivedLevelData.options.flat
    }

    override fun getSeed(): Long {
        return this.derivedLevelData.options.seed
    }

    override fun setRespawnData(respawnData: LevelData.RespawnData) {
        if (this.properties.respawnData.isPresent) {
            this.properties.respawnData = Optional.of(respawnData)
            return
        }
        super.setRespawnData(respawnData)
    }

    override fun getRespawnData(): LevelData.RespawnData {
        return this.properties.respawnData.orElseGet { super.getRespawnData() }
    }

    override fun getWeatherData(): WeatherData {
        return this.properties.weather.orElseGet { super.weatherData }
    }

    override fun getGameRules(): GameRules {
        return this.derivedLevelData.properties.gameRules.getOrElse { super.gameRules }
    }

    override fun save(progress: ProgressListener?, flush: Boolean, skip: Boolean) {
        if (!this.persistence.shouldSave()) {
            return
        }
        super.save(progress, flush, skip)

        try {
            ArcadeUtils.scopedProblemReporter { reporter ->
                val output = TagValueOutput.createWithContext(reporter, this.registryAccess())
                output.store("factory", CustomLevelFactory.CODEC, this.factory)
                NbtUtils.addCurrentDataVersion(output)
                val path = getDimensionDataPath(this.server(), this.dimension())
                path.createParentDirectories()
                NbtIo.write(output.buildResult(), path)
            }
        } catch (e: IllegalStateException) {
            ArcadeUtils.logger.error("Failed to encode custom level data", e)
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to write custom level data", e)
        }
    }

    protected open fun loadCustomSpawners() {
        val spawners = (this as ServerLevelAccessor).arcade_getCustomSpawners()
        for (factory in this.options.customSpawners) {
            spawners.add(factory.create(this))
        }
    }

    protected open fun loadForcedChunks() {
        val forced = this.dataStorage.get(TicketStorage.TYPE)
        forced?.activateAllDeactivatedTickets()
    }

    public companion object {
        /**
         * This reads the custom dimension data for a given
         * [dimension] and re-constructs the [CustomLevel].
         * This may return `null` if there is no dimension data
         * available, or if an error occurs when reading the data.
         *
         * This **does not** add the [CustomLevel] to the [MinecraftServer].
         * If you're trying to load the dimension onto the server,
         * you probably want to call [ArcadeDimensions.load].
         *
         * @param server The [MinecraftServer] instance.
         * @param dimension The key for the dimension you're trying to read.
         */
        @JvmStatic
        public fun read(server: MinecraftServer, dimension: ResourceKey<Level>): CustomLevel? {
            val path = this.getDimensionDataPath(server, dimension)
            try {
                val compound = this.readDimensionData(server, path) ?: return null
                ArcadeUtils.scopedProblemReporter { reporter ->
                    val input = TagValueInput.create(reporter, server.registryAccess(), compound)
                    val factory = input.read("factory", CustomLevelFactory.CODEC).orElseThrow()
                    return factory.create(server, dimension)
                }
            } catch (e: Exception) {
                ArcadeUtils.logger.error("Failed to load custom level data", e)
                return null
            }
        }

        private fun readDimensionData(server: MinecraftServer, path: Path): CompoundTag? {
            val compound = NbtIo.read(path) ?: return null
            var version = NbtUtils.getDataVersion(compound)
            if (version == -1) {
                version = 4440

                val properties = compound.getCompound("factory")
                    .flatMap { factory -> factory.getCompound("properties") }
                    .getOrNull() ?: return compound
                val rules = properties.getCompound("game_rules").getOrNull() ?: return compound

                val copy = CompoundTag()
                copy.put("GameRules", rules)
                val fixed = DataFixTypes.LEVEL.updateToCurrentVersion(server.fixerUpper, copy, version)
                properties.put("game_rules", fixed.getCompoundOrEmpty("game_rules"))
                return compound
            }
            return compound
        }

        private fun getDimensionDataPath(server: MinecraftServer, dimension: ResourceKey<Level>): Path {
            return server.getDimensionDataPath(dimension, "arcade").resolve("custom_dimension_data.dat")
        }
    }
}