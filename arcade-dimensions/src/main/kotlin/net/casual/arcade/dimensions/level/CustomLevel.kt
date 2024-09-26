package net.casual.arcade.dimensions.level

import net.casual.arcade.dimensions.ArcadeDimensions
import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.dimensions.level.factory.SimpleCustomLevelFactory
import net.casual.arcade.dimensions.mixins.level.MinecraftServerAccessor
import net.casual.arcade.dimensions.mixins.level.ServerLevelAccessor
import net.casual.arcade.dimensions.utils.getDimensionPath
import net.casual.arcade.dimensions.utils.impl.DerivedLevelData
import net.casual.arcade.dimensions.utils.impl.NullChunkProgressListener
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.get
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.RegistryOps
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ProgressListener
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import net.minecraft.world.level.biome.BiomeManager
import net.minecraft.world.level.storage.LevelData
import org.apache.commons.io.file.PathUtils
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.Executor
import kotlin.io.path.createParentDirectories

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
    dispatcher: Executor = (server as MinecraftServerAccessor).executor,
): ServerLevel(
    server,
    dispatcher,
    (server as MinecraftServerAccessor).storage,
    DerivedLevelData(properties, options, server.worldData, server.worldData.overworldData()),
    key,
    options.stem.value(),
    NullChunkProgressListener,
    options.debug,
    BiomeManager.obfuscateSeed(options.seed),
    ArrayList(),
    options.tickTime,
    null
) {
    private val derivedLevelData: DerivedLevelData
        get() = this.levelData as DerivedLevelData

    init {
        this.loadCustomSpawners()

        // In case of server crash, we should still delete temporary levels
        if (!this.persistence.shouldSave()) {
            PathUtils.deleteOnExit(server.getDimensionPath(key))
        }
    }

    override fun tickTime() {
        // super.tickTime() ticks the global scheduler
        if (this.options.tickTime && this.gameRules.get(GameRules.RULE_DAYLIGHT)) {
            ++this.dayTime
        }
    }

    // We cannot reference `this.options`, as these methods
    // may be called during the super constructor

    override fun isFlat(): Boolean {
        return this.derivedLevelData.options.flat
    }

    override fun getSeed(): Long {
        return this.derivedLevelData.options.seed
    }

    override fun save(progress: ProgressListener?, flush: Boolean, skip: Boolean) {
        if (!this.persistence.shouldSave()) {
            return
        }
        super.save(progress, flush, skip)

        try {
            val compound = CompoundTag()
            val ops = RegistryOps.create(NbtOps.INSTANCE, this.registryAccess())
            compound.put("factory", CustomLevelFactory.CODEC.encodeStart(ops, this.factory).orThrow)

            val path = getDimensionDataPath(this.server, this.dimension())
            path.createParentDirectories()
            NbtIo.write(compound, path)
        } catch (e: IllegalStateException) {
            ArcadeUtils.logger.error("Failed to encode custom level data", e)
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to write custom level data", e)
        }
    }

    protected open fun loadCustomSpawners() {
        val spawners = (this as ServerLevelAccessor).customSpawners
        for (factory in this.options.customSpawners) {
            spawners.add(factory.create(this))
        }
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
                val compound = NbtIo.read(path) ?: return null
                val ops = RegistryOps.create(NbtOps.INSTANCE, server.registryAccess())
                val factory = CustomLevelFactory.CODEC.parse(ops, compound.get("factory")).orThrow
                return factory.create(server, dimension)
            } catch (e: Exception) {
                ArcadeUtils.logger.error("Failed to load custom level data", e)
                return null
            }
        }

        private fun getDimensionDataPath(server: MinecraftServer, dimension: ResourceKey<Level>): Path {
            return server.getDimensionPath(dimension).resolve("arcade-dimension-data.nbt")
        }
    }
}