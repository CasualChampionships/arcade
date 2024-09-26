package net.casual.arcade.test

import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.builder.DimensionTypeBuilder
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevelsBuilder
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.registry.RegistryEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TimeUtils.Minutes
import net.fabricmc.api.ModInitializer
import net.minecraft.core.Registry
import net.minecraft.core.registries.Registries
import net.minecraft.resources.ResourceKey

object ArcadeTest: ModInitializer {
    private val customDimensionKey = ResourceKey.create(Registries.DIMENSION_TYPE, ResourceUtils.arcade("custom_dim_type"))

    override fun onInitialize() {
        RegistryEventHandler.register(Registries.DIMENSION_TYPE) { (registry) ->
            Registry.register(registry, this.customDimensionKey, DimensionTypeBuilder.build {
                bedWorks = false
                respawnAnchorWorks = true
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
                }
                set(VanillaDimension.Nether) {
                    dimensionKey(ResourceUtils.arcade("custom_nether"))
                }
            }
            for (level in levels.all()) {
                server.addCustomLevel(level)
            }

            // server.addCustomLevel {
            //     dimensionKey(ResourceUtils.arcade("custom_dimension"))
            //     dimensionType {
            //         bedWorks = false
            //         respawnAnchorWorks = true
            //     }
            //     chunkGenerator = VoidChunkGenerator(server)
            //     timeOfDay = 15.Minutes.ticks.toLong()
            //     persistence = LevelPersistence.Persistent
            // }
            server.addCustomLevel {
                dimensionKey(ResourceUtils.arcade("custom_dimension_with_height"))
                dimensionType(customDimensionKey)
                chunkGenerator = VoidChunkGenerator(server)
                timeOfDay = 15.Minutes.ticks.toLong()
            }
        }
    }
}