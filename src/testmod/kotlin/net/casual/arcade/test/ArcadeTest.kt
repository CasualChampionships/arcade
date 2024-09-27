package net.casual.arcade.test

import com.mojang.serialization.MapCodec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.casual.arcade.dimensions.level.CustomLevel
import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.DimensionTypeBuilder
import net.casual.arcade.dimensions.level.factory.CustomLevelFactory
import net.casual.arcade.dimensions.level.spawner.CatSpawnerFactory
import net.casual.arcade.dimensions.level.spawner.CustomSpawnerFactory
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevelsBuilder
import net.casual.arcade.dimensions.utils.DimensionRegistries
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.dimensions.utils.loadOrAddCustomLevel
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.registry.RegistryEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TimeUtils.Minutes
import net.casual.arcade.utils.codec.CodecProvider
import net.casual.arcade.utils.codec.CodecProvider.Companion.register
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.MinecraftServer
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.CustomSpawner
import net.minecraft.world.level.Level

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
        val dimensionTypeKey = ResourceKey.create(
            Registries.DIMENSION_TYPE,
            ResourceLocation.withDefaultNamespace("foo")
        )

        RegistryEventHandler.register(Registries.DIMENSION_TYPE) { (registry) ->
            Registry.register(registry, dimensionTypeKey, DimensionTypeBuilder.build {
                bedWorks = false
                piglinSafe = true
                height = 512
                // ...
            })
        }

        GlobalEventHandler.register<ServerLoadedEvent> { (server) ->
            val levels = VanillaLikeLevelsBuilder.build(server) {
                set(VanillaDimension.Overworld) {
                    dimensionKey(ResourceUtils.arcade("custom_overworld"))
                    weather {
                        rainTime = 20.Minutes.ticks
                        raining = true
                    }
                    persistence = LevelPersistence.Permanent
                }
                set(VanillaDimension.Nether) {
                    dimensionKey(ResourceUtils.arcade("custom_nether"))
                    persistence = LevelPersistence.Permanent
                }
            }
            for (level in levels.all()) {
                server.addCustomLevel(level)
            }

            server.loadOrAddCustomLevel(ResourceUtils.arcade("custom_dimension")) {
                dimensionType {
                    bedWorks = false
                    piglinSafe = true
                }
                chunkGenerator = VoidChunkGenerator(server)
            }
        }
    }
}