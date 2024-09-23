package net.casual.arcade.dimensions.utils

import net.casual.arcade.dimensions.border.LevelSpecificBorderBroadcaster
import net.casual.arcade.dimensions.mixins.fantasy.FantasyInvoker
import net.casual.arcade.dimensions.vanilla.VanillaDimension.*
import net.casual.arcade.dimensions.vanilla.VanillaLikeDimensions
import net.casual.arcade.dimensions.vanilla.VanillaLikeRuntimeLevel
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerStoppingEvent
import net.casual.arcade.extensions.event.LevelExtensionEvent
import net.casual.arcade.utils.ServerUtils
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.dimension.BuiltinDimensionTypes
import net.minecraft.world.level.dimension.LevelStem
import xyz.nucleoid.fantasy.Fantasy
import xyz.nucleoid.fantasy.RuntimeWorld
import xyz.nucleoid.fantasy.RuntimeWorldConfig
import xyz.nucleoid.fantasy.RuntimeWorldHandle

public object FantasyUtils {
    public fun createTemporaryVanillaLikeLevels(
        server: MinecraftServer = ServerUtils.getServer(),
        seed: Long
    ): VanillaLikeLevels {
        val overworldConfig = this.createOverworldConfig(server, seed)
        val netherConfig = this.createNetherConfig(server, seed)
        val endConfig = this.createEndConfig(server, seed)
        return this.createTemporaryVanillaLikeLevels(server, overworldConfig, netherConfig, endConfig)
    }

    public fun createTemporaryVanillaLikeLevels(
        server: MinecraftServer = ServerUtils.getServer(),
        overworldConfig: RuntimeWorldConfig,
        netherConfig: RuntimeWorldConfig,
        endConfig: RuntimeWorldConfig
    ): VanillaLikeLevels {
        val fantasy = Fantasy.get(server)
        val dimensions = this.setWorldConstructors(overworldConfig, netherConfig, endConfig)

        val overworld = fantasy.openTemporaryWorld(overworldConfig)
        val nether = fantasy.openTemporaryWorld(netherConfig)
        val end = fantasy.openTemporaryWorld(endConfig)

        this.setOtherDimensions(dimensions, overworld, nether, end)
        return VanillaLikeLevels(overworld, nether, end)
    }

    public fun createPersistentVanillaLikeLevels(
        server: MinecraftServer = ServerUtils.getServer(),
        overworldId: ResourceLocation,
        netherId: ResourceLocation,
        endId: ResourceLocation,
        seed: Long
    ): VanillaLikeLevels {
        return this.createPersistentVanillaLikeLevels(
            server,
            PersistentConfig(overworldId, this.createOverworldConfig(server, seed)),
            PersistentConfig(netherId, this.createNetherConfig(server, seed)),
            PersistentConfig(endId, this.createEndConfig(server, seed))
        )
    }

    public fun createPersistentVanillaLikeLevels(
        server: MinecraftServer = ServerUtils.getServer(),
        overworldConfig: PersistentConfig,
        netherConfig: PersistentConfig,
        endConfig: PersistentConfig
    ): VanillaLikeLevels {
        val fantasy = Fantasy.get(server)
        val dimensions = this.setWorldConstructors(overworldConfig.config, netherConfig.config, endConfig.config)

        val overworld = fantasy.getOrOpenPersistentWorld(overworldConfig.location, overworldConfig.config)
        val nether = fantasy.getOrOpenPersistentWorld(netherConfig.location, netherConfig.config)
        val end = fantasy.getOrOpenPersistentWorld(endConfig.location, endConfig.config)

        this.setOtherDimensions(dimensions, overworld, nether, end)
        return VanillaLikeLevels(overworld, nether, end)
    }

    public fun createOverworldConfig(server: MinecraftServer, seed: Long = 0L): RuntimeWorldConfig {
        val stem = server.registryAccess()
            .registryOrThrow(Registries.LEVEL_STEM)
            .getOrThrow(LevelStem.OVERWORLD)
        return RuntimeWorldConfig()
            .setSeed(seed)
            .setShouldTickTime(true)
            .setDimensionType(BuiltinDimensionTypes.OVERWORLD)
            .setGenerator(stem.generator)
    }

    public fun createNetherConfig(server: MinecraftServer, seed: Long = 0L): RuntimeWorldConfig {
        val stem = server.registryAccess()
            .registryOrThrow(Registries.LEVEL_STEM)
            .getOrThrow(LevelStem.NETHER)
        return RuntimeWorldConfig()
            .setSeed(seed)
            .setDimensionType(BuiltinDimensionTypes.NETHER)
            .setGenerator(stem.generator)
    }

    public fun createEndConfig(server: MinecraftServer, seed: Long = 0L): RuntimeWorldConfig {
        val stem = server.registryAccess()
            .registryOrThrow(Registries.LEVEL_STEM)
            .getOrThrow(LevelStem.END)
        return RuntimeWorldConfig()
            .setSeed(seed)
            .setDimensionType(BuiltinDimensionTypes.END)
            .setGenerator(stem.generator)
    }

    private fun setWorldConstructors(
        overworld: RuntimeWorldConfig,
        nether: RuntimeWorldConfig,
        end: RuntimeWorldConfig
    ): VanillaLikeDimensions {
        val others = VanillaLikeDimensions()
        overworld.setWorldConstructor(VanillaLikeRuntimeLevel.constructor(Overworld, others))
        nether.setWorldConstructor(VanillaLikeRuntimeLevel.constructor(Nether, others))
        end.setWorldConstructor(VanillaLikeRuntimeLevel.constructor(End, others))
        return others
    }

    private fun setOtherDimensions(
        dimensions: VanillaLikeDimensions,
        overworld: RuntimeWorldHandle,
        nether: RuntimeWorldHandle,
        end: RuntimeWorldHandle
    ) {
        dimensions.overworld = overworld.registryKey
        dimensions.nether = nether.registryKey
        dimensions.end = end.registryKey
    }

    public class PersistentConfig(
        public val location: ResourceLocation,
        public val config: RuntimeWorldConfig
    )

    public data class VanillaLikeLevels(
        val overworldHandle: RuntimeWorldHandle,
        val netherHandle: RuntimeWorldHandle,
        val endHandle: RuntimeWorldHandle
    ) {
        val overworld: ServerLevel
            get() = this.overworldHandle.asWorld()
        val nether: ServerLevel
            get() = this.netherHandle.asWorld()
        val end: ServerLevel
            get() = this.endHandle.asWorld()

        public fun levels(): Triple<ServerLevel, ServerLevel, ServerLevel> {
            return Triple(this.overworld, this.nether, this.end)
        }
    }

    internal fun registerEvents() {
        GlobalEventHandler.register<ServerStoppingEvent>(priority = Int.MAX_VALUE) {
            (Fantasy.get(it.server) as FantasyInvoker).doTick()
        }
        GlobalEventHandler.register<LevelExtensionEvent> { (level) ->
            if (level is RuntimeWorld) {
                level.worldBorder.addListener(LevelSpecificBorderBroadcaster(level))
            }
        }
    }
}