package net.casual.arcade.test

import net.casual.arcade.dimensions.level.LevelPersistence
import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevelsBuilder
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.dimensions.utils.impl.VoidChunkGenerator
import net.casual.arcade.dimensions.utils.loadOrAddCustomLevel
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TimeUtils.Minutes
import net.fabricmc.api.ModInitializer

object ArcadeTest: ModInitializer {
    override fun onInitialize() {
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