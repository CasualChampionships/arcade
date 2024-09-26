package net.casual.arcade.dimensions.level

import net.casual.arcade.dimensions.level.builder.CustomLevelBuilder
import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.dimensions.level.factory.SimpleCustomLevelFactory
import net.casual.arcade.dimensions.mixins.level.MinecraftServerAccessor
import net.casual.arcade.dimensions.utils.GenerationOptionsContext
import net.casual.arcade.dimensions.utils.LevelPersistenceTracker
import net.casual.arcade.dimensions.utils.getDimensionPath
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
import org.apache.commons.io.file.PathUtils
import java.io.IOException
import java.nio.file.Path
import java.util.concurrent.Executor
import kotlin.io.path.createParentDirectories

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
    DerivedLevelData(properties, server.worldData, server.worldData.overworldData()),
    key,
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

    override fun isFlat(): Boolean {
        return this.options.flat
    }

    @Suppress("SENSELESS_COMPARISON")
    override fun getSeed(): Long {
        // options can technically be null due to leaking 'this'
        if (this.options == null) {
            return GenerationOptionsContext.get(this.server).seed
        }
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
        } catch (e: IllegalStateException) {
            ArcadeUtils.logger.error("Failed to encode custom level data", e)
        } catch (e: IOException) {
            ArcadeUtils.logger.error("Failed to write custom level data", e)
        }
    }

    public companion object {
        @JvmStatic
        public fun read(server: MinecraftServer, dimension: ResourceKey<Level>): CustomLevel? {
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

        @JvmStatic
        public fun create(server: MinecraftServer, block: CustomLevelBuilder.() -> Unit): CustomLevel {
            val builder = CustomLevelBuilder()
            builder.block()
            return builder.build(server)
        }

        private fun getDimensionDataPath(server: MinecraftServer, dimension: ResourceKey<Level>): Path {
            return server.getDimensionPath(dimension).resolve("arcade-dimension-data.nbt")
        }
    }
}