package net.casual.arcade.test

import net.casual.arcade.dimensions.level.vanilla.VanillaDimension
import net.casual.arcade.dimensions.level.vanilla.VanillaLikeLevels
import net.casual.arcade.dimensions.utils.addCustomLevel
import net.casual.arcade.events.GlobalEventHandler
import net.casual.arcade.events.server.ServerLoadedEvent
import net.casual.arcade.utils.ResourceUtils
import net.casual.arcade.utils.TimeUtils.Minutes
import net.fabricmc.api.ModInitializer
import xyz.nucleoid.fantasy.util.VoidChunkGenerator

object ArcadeTest: ModInitializer {
    override fun onInitialize() = GlobalEventHandler.register<ServerLoadedEvent> { (server) ->
        val levels = VanillaLikeLevels.create(server) {
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

        server.addCustomLevel {
            dimensionKey(ResourceUtils.arcade("custom_dimension"))
            dimensionType {
                bedWorks = false
                respawnAnchorWorks = true
            }
            chunkGenerator = VoidChunkGenerator(server)
            timeOfDay = 15.Minutes.ticks.toLong()
        }
    }
}