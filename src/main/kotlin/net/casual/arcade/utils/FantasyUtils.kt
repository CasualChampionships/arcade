package net.casual.arcade.utils

import net.casual.arcade.Arcade
import net.casual.arcade.level.VanillaDimension.*
import net.casual.arcade.level.VanillaLikeDimensions
import net.casual.arcade.level.VanillaLikeRuntimeLevel
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.fantasy.RuntimeWorldHandle
import kotlin.random.Random

public object FantasyUtils {
    public fun createTemporaryVanillaLikeLevels(
        server: MinecraftServer = Arcade.getServer(),
        overworldConfig: RuntimeWorldConfig,
        netherConfig: RuntimeWorldConfig,
        endConfig: RuntimeWorldConfig
    ): Triple<RuntimeWorldHandle, RuntimeWorldHandle, RuntimeWorldHandle> {
        val fantasy = Fantasy.get(server)
        val dimensions = this.setWorldConstructors(overworldConfig, netherConfig, endConfig)

        val overworld = fantasy.openTemporaryWorld(overworldConfig)
        val nether = fantasy.openTemporaryWorld(netherConfig)
        val end = fantasy.openTemporaryWorld(endConfig)

        this.setOtherDimensions(dimensions, overworld, nether, end)
        return Triple(overworld, nether, end)
    }

    public fun createNullableTemporaryVanillaLikeLevels(
        server: MinecraftServer = Arcade.getServer(),
        overworldConfig: RuntimeWorldConfig? = null,
        netherConfig: RuntimeWorldConfig? = null,
        endConfig: RuntimeWorldConfig? = null
    ): Triple<RuntimeWorldHandle?, RuntimeWorldHandle?, RuntimeWorldHandle?> {
        val fantasy = Fantasy.get(server)
        val dimensions = this.setWorldConstructors(overworldConfig, netherConfig, endConfig)

        val overworld = overworldConfig?.let { fantasy.openTemporaryWorld(it) }
        val nether = netherConfig?.let { fantasy.openTemporaryWorld(it) }
        val end = endConfig?.let { fantasy.openTemporaryWorld(it) }

        this.setOtherDimensions(dimensions, overworld, nether, end)
        return Triple(overworld, nether, end)
    }

    public fun createPersistentVanillaLikeLevels(
        server: MinecraftServer = Arcade.getServer(),
        overworldConfig: PersistentConfig,
        netherConfig: PersistentConfig,
        endConfig: PersistentConfig
    ): Triple<RuntimeWorldHandle, RuntimeWorldHandle, RuntimeWorldHandle> {
        val fantasy = Fantasy.get(server)
        val dimensions = this.setWorldConstructors(overworldConfig.config, netherConfig.config, endConfig.config)

        val overworld = fantasy.getOrOpenPersistentWorld(overworldConfig.location, overworldConfig.config)
        val nether = fantasy.getOrOpenPersistentWorld(netherConfig.location, netherConfig.config)
        val end = fantasy.getOrOpenPersistentWorld(endConfig.location, endConfig.config)

        this.setOtherDimensions(dimensions, overworld, nether, end)
        return Triple(overworld, nether, end)
    }

    public fun createNullablePersistentVanillaLikeLevels(
        server: MinecraftServer = Arcade.getServer(),
        overworldConfig: PersistentConfig? = null,
        netherConfig: PersistentConfig? = null,
        endConfig: PersistentConfig? = null
    ): Triple<RuntimeWorldHandle?, RuntimeWorldHandle?, RuntimeWorldHandle?> {
        val fantasy = Fantasy.get(server)
        val dimensions = this.setWorldConstructors(overworldConfig?.config, netherConfig?.config, endConfig?.config)

        val overworld = overworldConfig?.let { fantasy.getOrOpenPersistentWorld(it.location, it.config) }
        val nether = netherConfig?.let { fantasy.getOrOpenPersistentWorld(it.location, it.config) }
        val end = endConfig?.let { fantasy.getOrOpenPersistentWorld(it.location, it.config) }

        this.setOtherDimensions(dimensions, overworld, nether, end)
        return Triple(overworld, nether, end)
    }

    public fun createOverworldConfig(seed: Long = 0L): RuntimeWorldConfig {
        return RuntimeWorldConfig()
            .setSeed(seed)
            .setShouldTickTime(true)
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(LevelUtils.overworld().chunkSource.generator)
    }

    public fun createNetherConfig(seed: Long = 0L): RuntimeWorldConfig {
        return RuntimeWorldConfig()
            .setSeed(seed)
            .setDimensionType(BuiltinDimensionTypes.NETHER)
            .setGenerator(LevelUtils.nether().chunkSource.generator)
    }

    public fun createEndConfig(seed: Long = 0L): RuntimeWorldConfig {
        return RuntimeWorldConfig()
            .setSeed(seed)
            .setDimensionType(BuiltinDimensionTypes.END)
            .setGenerator(LevelUtils.end().chunkSource.generator)
    }

    private fun setWorldConstructors(
        overworld: RuntimeWorldConfig? = null,
        nether: RuntimeWorldConfig? = null,
        end: RuntimeWorldConfig? = null
    ): VanillaLikeDimensions {
        val others = VanillaLikeDimensions()
        overworld?.setWorldConstructor(VanillaLikeRuntimeLevel.constructor(Overworld, others))
        nether?.setWorldConstructor(VanillaLikeRuntimeLevel.constructor(Nether, others))
        end?.setWorldConstructor(VanillaLikeRuntimeLevel.constructor(End, others))
        return others
    }

    private fun setOtherDimensions(
        dimensions: VanillaLikeDimensions,
        overworld: RuntimeWorldHandle?,
        nether: RuntimeWorldHandle?,
        end: RuntimeWorldHandle?
    ) {
        dimensions.overworld = overworld?.registryKey
        dimensions.nether = nether?.registryKey
        dimensions.end = end?.registryKey
    }

    public class PersistentConfig(
        public val location: ResourceLocation,
        public val config: RuntimeWorldConfig
    )
}