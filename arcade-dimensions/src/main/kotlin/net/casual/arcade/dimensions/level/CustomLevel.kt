package net.casual.arcade.dimensions.level

import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.dimensions.level.factory.SimpleCustomLevelFactory
import net.casual.arcade.dimensions.mixins.level.MinecraftServerAccessor
import net.casual.arcade.dimensions.utils.GenerationOptionsContext
import net.casual.arcade.dimensions.utils.LevelPersistenceTracker
import net.casual.arcade.dimensions.utils.impl.DerivedLevelData
import net.casual.arcade.dimensions.utils.impl.NullChunkProgressListener
import net.casual.arcade.utils.ArcadeUtils
import net.casual.arcade.utils.get
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.NbtIo
import net.minecraft.nbt.NbtOps
import net.minecraft.resources.ResourceKey
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.util.ProgressListener
import net.minecraft.world.level.GameRules
import net.minecraft.world.level.Level
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.Executor
import kotlin.io.path.createParentDirectories

public open class CustomLevel(
    server: MinecraftServer,
    dimension: ResourceKey<Level>,
    public val properties: LevelProperties,
    public val options: LevelGenerationOptions,
    public val persistence: LevelPersistence = LevelPersistence.Temporary,
    private val factory: CustomLevelFactory = SimpleCustomLevelFactory(properties, options, persistence),
    dispatcher: Executor = (server as MinecraftServerAccessor).executor,
): ServerLevel(
    server,
    dispatcher,
    (server as MinecraftServerAccessor).storage,
    DerivedLevelData(properties, server.worldData, server.worldData.overworldData()),
    dimension,
    options.stem,
    NullChunkProgressListener,
    options.debug,
    GenerationOptionsContext.set(server, options),
    listOf(), // TODO: Implement CustomSpawner support
    options.tickTime,
    null
) {
    init {
        GenerationOptionsContext.reset(server)
    }

    override fun tickTime() {
        // super.tickTime() ticks the global scheduler
        if (this.options.tickTime && this.gameRules.get(GameRules.RULE_DAYLIGHT)) {
            ++this.dayTime
        }
    }

    override fun isFlat(): Boolean {
        return this.options.flat
    }

    override fun getSeed(): Long {
        return this.options.seed
    }

    override fun save(progress: ProgressListener?, flush: Boolean, skip: Boolean) {
        if (!this.persistence.shouldSave()) {
            return
        }
        super.save(progress, flush, skip)

        try {
            val compound = CompoundTag()
            compound.put("factory", CustomLevelFactory.CODEC.encodeStart(NbtOps.INSTANCE, this.factory).orThrow)

            val path = getDimensionDataPath(this.server, this.dimension())
            path.createParentDirectories()
            NbtIo.write(compound, path)

            if (this.persistence == LevelPersistence.Persistent) {
                LevelPersistenceTracker.mark(this.dimension())
            }
        } catch (e: IllegalStateException) {
            ArcadeUtils.logger.error("Failed to encode custom level data", e)
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to write custom level data", e)
        }
    }

    public companion object {
        public fun load(server: MinecraftServer, dimension: ResourceKey<Level>): CustomLevel? {
            val path = this.getDimensionDataPath(server, dimension)
            try {
                val compound = NbtIo.read(path) ?: return null
                val factory = CustomLevelFactory.CODEC.parse(NbtOps.INSTANCE, compound.get("factory")).orThrow
                return factory.create(server, dimension)
            } catch (e: Exception) {
                ArcadeUtils.logger.error("Failed to load custom level data", e)
                return null
            }
        }

        private fun getDimensionDataPath(server: MinecraftServer, dimension: ResourceKey<Level>): Path {
            return (server as MinecraftServerAccessor).storage.getDimensionPath(dimension)
                .resolve("arcade-dimension-data.nbt")
        }
    }
}